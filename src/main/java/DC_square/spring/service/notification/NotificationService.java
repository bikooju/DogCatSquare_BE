package DC_square.spring.service.notification;

import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.notification.UserNotification;
import DC_square.spring.domain.enums.NotificationType;
import DC_square.spring.repository.NotificationRepository;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.web.dto.request.notification.ReadNotificationRequestDto;
import DC_square.spring.web.dto.response.notification.NotificationDeliveryResponseDto;
import DC_square.spring.web.dto.response.notification.NotificationInfoDto;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.Notification;
import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


/**
 * FCM 발송 + 알람 이력(Notification) 저장 - 예약 큐(DdayAlarmReservation)는 별도 테이블에서 관리 - 해당 서비스는 알람 이력을
 * 저장하는거임(Notification)
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

  private final NotificationRepository notificationRepository;
  private final UserRepository userRepository;

  // 사용자별 emitter 목록
  // 레지스트리 : 누가 어떤 SSE 연결을 갖고 있는가를 보관(특정 유저의 SSE 연결을 기억하고 있는 저장소)
  private final Map<Long, List<SseEmitter>> userEmitters = new ConcurrentHashMap<>();

  //emitter별 keep-alive task
  //emitter마다 등록된 주기적으로 서버가 살아있다는 신호를 보내는 하트비트 스케줄 작업을
  // 나중에 멈출 수 있도록 ScheduledFuture 객체를 저장해두는 용도
  private final Map<SseEmitter, ScheduledFuture<?>> keepAliveTasks = new ConcurrentHashMap<>();

  // 공용 스케줄러(스레드풀 2)
  private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(
      Math.max(2, Runtime.getRuntime().availableProcessors() / 2));

  // 타임아웃: 0L = 무제한
  private static final long SSE_TIMEOUT_MS = 0L;
  private static final long HEARTBEAT_INTERVAL_SECONDS = 15L; // 15초

  /**
   * [통합 진입점] 알림 저장 + 라우팅(SSE 우선, 없으면 FCM)
   *
   * @param notificationType 알림 유형
   * @param targetUser       알림 대상 사용자 엔티티
   * @param content          알림 내용
   * @return FCM 시도/결과 등 메타 포함 응답 DTO
   */
  @Transactional
  public NotificationDeliveryResponseDto sendNotificationAndSave(
      NotificationType notificationType,
      User targetUser,
      String content
  ) {
    boolean attempted = false; // FCM 전송 시도 여부
    boolean success = false; // FCM 전송 성공 여부
    String messageId = null;  // FCM 메시지 ID
    String error = null;  // FCM 에러 메시지

    // 1) 알림 이력 저장(항상 먼저)
    UserNotification userNotification = UserNotification.builder()
        .user(targetUser)
        .notificationType(notificationType)
        .content(content)
        .isRead(false)
        .build();
    notificationRepository.save(userNotification);

    // 2) 라우팅: SSE 있으면 afterCommit에서 SSE 전송, 없으면 즉시 FCM 시도
    Long userId = targetUser.getId();
    if (hasActiveEmitters(userId)) {
      // 트랜잭션 커밋 후에만 전송(일관성 보장)
      TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
        @Override
        public void afterCommit() {
          // DB에 성공적으로 저장된 이후에 SSE로 알림 보내기
          sendToAllEmitters(NotificationInfoDto.of(userNotification));
        }
      });
    } else {
      // SSE 연결 없음 → FCM 시도
      String token = targetUser.getFcmToken();
      if (token == null || token.isBlank()) {
        log.warn("No FCM token. userId={}", userId);
      } else {
        attempted = true;
        try {
          Message message = Message.builder()
              .setToken(token)
              .setNotification(Notification.builder().setBody(content).build())
              .build();
          String response = FirebaseMessaging.getInstance().send(message);
          success = true;
          messageId = response;
          log.info("FCM sent. userId={}, response={}", userId, response);
        } catch (Exception e) {
          success = false;
          error = e.getMessage();
          log.error("FCM send failed. userId={}, error={}", userId, e.getMessage(), e);
        }
      }
    }

    // 3) 응답
    return NotificationDeliveryResponseDto.builder()
        .userId(userId)
        .notificationType(notificationType)
        .content(content)
        .fcmAttempted(attempted)
        .fcmSuccess(success)
        .fcmResponseId(messageId)
        .errorMessage(error)
        .notificationId(userNotification.getId())
        .createdAt(userNotification.getCreatedAt())
        .isRead(false)
        .build();
  }

  /**
   * FCM 토큰 등록/갱신
   */
  @Transactional
  public void registerFcmToken(Long userId, String fcmToken) {
    if (userId == null) {
      throw new IllegalArgumentException("userId가 필요합니다.");
    }
    if (fcmToken == null || fcmToken.isBlank()) {
      throw new IllegalArgumentException("유효한 fcmToken이 필요합니다.");
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 유저: " + userId));

    user.setFcmToken(fcmToken); // 더티체킹
    log.info("FCM 토큰 등록/갱신 완료. userId={}", userId);
  }

  /**
   * SSE 구독(클라이언트가 최초 연결 시 호출)
   */
  public SseEmitter subscribe(Long userId) {
    SseEmitter emitter = new SseEmitter(SSE_TIMEOUT_MS);

    // 사용자별 Emitter 등록
    userEmitters.computeIfAbsent(userId, key -> new CopyOnWriteArrayList<>()).add(emitter);

    // 연결 라이프사이클 훅
    emitter.onCompletion(() -> cleanup(userId, emitter, "completion"));
    emitter.onTimeout(() -> cleanup(userId, emitter, "timeout"));
    emitter.onError(e -> cleanup(userId, emitter, "error"));

    try {
      // 초기 신호
      emitter.send(SseEmitter.event()
          .name("INIT")
          .id(String.valueOf(Instant.now().toEpochMilli()))
          .data("connected as " + userId));

      // 미읽음 알림 비동기 밀어넣기
      sendUnreadNotifications(userId, emitter);

      // 주기적 PING(keep-alive)
      ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
        try {
          emitter.send(SseEmitter.event().name("PING").data("keep-alive"));
        } catch (IOException io) {
          log.debug("Keep-alive failed (closing) for user {}", userId);
          emitter.complete();
        } catch (Exception ex) {
          log.warn("Keep-alive unexpected error for user {}", userId);
          emitter.complete();
        }
      }, 0, HEARTBEAT_INTERVAL_SECONDS, TimeUnit.SECONDS);

      keepAliveTasks.put(emitter, future);

    } catch (IOException e) {
      log.error("Failed to send INIT to user {}: {}", userId, e.toString());
      emitter.completeWithError(e);
    }

    return emitter;
  }

  /**
   * (호환용) 기존 코드에서 쓰던 SSE 전용 메서드 → 내부적으로 통합 메서드로 위임하여 동일 정책 유지
   */
  @Transactional
  public void createAndSend(Long userId, NotificationType type, String message) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 사용자 정보를 찾을 수 없습니다."));
    // 통합 라우팅 메서드로 위임
    sendNotificationAndSave(type, user, message);
  }

  /**
   * 미읽음 알림 조회
   */
  @Transactional(readOnly = true)
  public List<NotificationInfoDto> getUnreadNotifications(Long userId) {
    return notificationRepository
        .findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
        .stream()
        .map(NotificationInfoDto::of)
        .collect(Collectors.toList());
  }

  /**
   * 읽음 처리(전체 또는 일부)
   */
  @Transactional
  public void readNotifications(Long userId, ReadNotificationRequestDto requestDto) {
    if (requestDto == null || requestDto.getIds() == null) {
      notificationRepository.bulkMarkReadAll(userId);
      return;
    }
    List<Long> distinctIds = requestDto.getIds().stream().distinct().toList();
    if (distinctIds.isEmpty()) {
      notificationRepository.bulkMarkReadAll(userId);
    } else {
      notificationRepository.bulkMarkReadByIds(userId, distinctIds);
    }
  }


  /**
   * 구독 직후 미읽음 알림 비동기 전송 - @Async 사용을 위해 @EnableAsync 필요
   */
  @Async
  public void sendUnreadNotifications(Long userId, SseEmitter emitter) {
    try {
      List<NotificationInfoDto> unread = getUnreadNotifications(userId);
      if (!unread.isEmpty()) {
        for (NotificationInfoDto dto : unread) {
          sendToEmitter(userId, emitter, dto);
        }
        log.debug("사용자 {}에게 미읽음 알림 {}개 전송.", userId, unread.size());
      }
    } catch (Exception e) {
      log.error("SSE 미읽음 전송 실패: {}", e.getMessage());
      emitter.completeWithError(e);
      cleanup(userId, emitter, "sendUnread failed");
    }
  }

  /**
   * 개별 Emitter로 전송
   */
  private void sendToEmitter(Long userId, SseEmitter emitter, NotificationInfoDto dto) {
    try {
      emitter.send(SseEmitter.event()
          .id(String.valueOf(dto.getId()))
          .name("NOTIFICATION")
          .data(dto));
    } catch (Exception e) {
      emitter.complete();
      cleanup(userId, emitter, "send failed");
    }
  }

  /**
   * Emitter/하트비트 정리
   */
  private void cleanup(Long userId, SseEmitter emitter, String reason) {
    try {
      Optional.ofNullable(keepAliveTasks.remove(emitter)).ifPresent(f -> f.cancel(true));
    } catch (Exception ignored) {
    }

    try {
      List<SseEmitter> list = userEmitters.get(userId);
      if (list != null) {
        list.remove(emitter);
        if (list.isEmpty()) {
          userEmitters.remove(userId, list);
        }
      }
    } catch (Exception ignored) {
    }

    log.debug("SSE cleanup done: user={}, reason={}", userId, reason);
  }

  /**
   * 현재 사용자에게 활성 Emitter가 존재하는지
   */
  private boolean hasActiveEmitters(Long userId) {
    List<SseEmitter> list = userEmitters.get(userId);
    return list != null && !list.isEmpty();
  }

  /**
   * 여러 Emitter로 브로드캐스트(성공 여부 반환) 브로드캐스트 = “같은 알림을, 여러 SSE 연결(여러 기기/탭)에 동시에 뿌리는 것”
   * sendToAllEmitters() = “그 유저의 모든 SSE 연결로 브로드캐스트하는 함수” -> 이게 필요한 이유는 동일한 사용자가 여러 탭을 띄어놓을 수도 있기
   * 때문에 한 유저의 여러 SSE 연결(여러기기/탭), 여러 Emitter에 동시에 뿌림 sendToEmitter() = “특정 SSE 연결 하나에만 보내는 함수
   */
  private boolean sendToAllEmitters(NotificationInfoDto dto) {
    Long userId = dto.getUserId();
    List<SseEmitter> emitters = userEmitters.getOrDefault(userId, List.of());
    boolean anySuccess = false;
    for (SseEmitter emitter : emitters) {
      try {
        emitter.send(SseEmitter.event()
            .id(String.valueOf(dto.getId()))
            .name("NOTIFICATION")
            .data(dto));
        anySuccess = true;
      } catch (Exception e) {
        emitter.complete();
        cleanup(userId, emitter, "send failed");
      }
    }
    return anySuccess;
  }

}