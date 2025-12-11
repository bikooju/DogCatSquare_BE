package DC_square.spring.service.dday;

import DC_square.spring.domain.entity.Dday;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.notification.DdayAlarmReservation;
import DC_square.spring.domain.enums.AlarmStatus;
import DC_square.spring.domain.enums.DdayType;
import DC_square.spring.domain.enums.NotificationType;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.repository.dday.DdayRepository;
import DC_square.spring.repository.notification.DdayAlarmReservationRepository;
import DC_square.spring.service.notification.NotificationService;
import DC_square.spring.web.dto.request.dday.DdayRequestDto;
import DC_square.spring.web.dto.request.dday.DdayUpdateRequestDto;
import DC_square.spring.web.dto.response.dday.DdayResponseDto;
import DC_square.spring.web.dto.response.notification.ToggleAlarmResponseDto;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * D-Day 서비스 - 기존 CRUD 기능 - 알림 예약/토글/발송 스케줄 처리
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DdayService {

  private static final ZoneId KST = ZoneId.of("Asia/Seoul");
  private static final int SEND_HOUR = 9;
  private static final int SEND_MINUTE = 0;

  private final DdayRepository ddayRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;
  private final DdayAlarmReservationRepository reservationRepository; // 알림 예약 레포지토리

  @Transactional
  public DdayResponseDto createDday(Long userId, DdayRequestDto request) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    Dday dday = request.toEntity(user);
    dday.setType(DdayType.CUSTOM);
    dday.setDefaultImageUrl();
    dday.setIsAlarm(false);

    Dday savedDday = ddayRepository.save(dday);
    return DdayResponseDto.from(savedDday);
  }

  @Transactional
  public List<DdayResponseDto> getDdaysByUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    List<Dday> ddays = ddayRepository.findAllByUserOrderByDayAsc(user);
    LocalDate today = LocalDate.now(KST);

    List<Dday> updatedDdays = new ArrayList<>();

    for (Dday dday : ddays) {
      if (dday.getTerm() != null &&
          dday.getTerm() > 0 &&
          dday.getDay().isBefore(today)) {

        LocalDate lastDday = dday.getDay();
        int maxIterations = 52;  // 최대 1년 (52주)
        int iterations = 0;

        while (lastDday.isBefore(today) && iterations < maxIterations) {
          lastDday = lastDday.plusWeeks(dday.getTerm());
          iterations++;
        }

        dday.setDay(lastDday);
        updatedDdays.add(dday);
      }
    }

    if (!updatedDdays.isEmpty()) {
      ddayRepository.saveAll(updatedDdays);
    }

    return ddays.stream()
        .map(DdayResponseDto::from)
        .collect(Collectors.toList());
  }

  @Transactional
  public DdayResponseDto updateDday(Long userId, Long ddayId, DdayUpdateRequestDto request) {
    Dday dday = ddayRepository.findById(ddayId)
        .orElseThrow(() -> new RuntimeException("D-day를 찾을 수 없습니다."));

    if (!dday.getUser().getId().equals(userId)) {
      throw new RuntimeException("수정 권한이 없습니다.");
    }

    if (request.getDay() != null) {
      dday.setDay(request.parseDay());
    }
    if (request.getTerm() != null) {
      dday.setTerm(request.getTerm());
    }
    if (request.getIsAlarm() != null) {
      dday.setIsAlarm(request.getIsAlarm());
    }

    Dday updatedDday = ddayRepository.save(dday);
    return DdayResponseDto.from(updatedDday);
  }


  @Transactional
  public void deleteDday(Long userId, Long ddayId) {
    Dday dday = ddayRepository.findById(ddayId)
        .orElseThrow(() -> new RuntimeException("D-day를 찾을 수 없습니다."));

    if (!dday.getUser().getId().equals(userId)) {
      throw new RuntimeException("삭제 권한이 없습니다.");
    }

    /**
     * Dday를 삭제하기 위해서는 해당 Dday에 발송되지 않은 예약 발송을 차단해야함 (PENDING -> Canceld)
     */
    // 대기상태 + 과거 알림 => 만기된 알림 조회 (보낼 수 있는 시각이 된 알림)
    List<DdayAlarmReservation> pendings = reservationRepository.findByDdayIdAndStatus(ddayId,
        AlarmStatus.PENDING);
    for (DdayAlarmReservation reservation : pendings) {
      reservation.setStatus(
          AlarmStatus.CANCELED); // 만기된 알림은 전부다 CANCELED로 처리 -> 더 이상 보내면 안되는 예약으로 처리
    }

    // D-day 자체를 삭제
    ddayRepository.delete(dday);
  }

  public List<Dday> getDdayEntitiesByUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    return ddayRepository.findAllByUserOrderByDayAsc(user);
  }

  // =========== 알림 토글/예약 ======================

  /**
   * 알림 토글(ON/OFF) - ON : 다음 발생일 기준으로 예약 1건(PENDING) 생성 - OFF : 해당 D-Day의 PENDING 예약 모두 CANCELED 처리
   *
   * @param userId
   * @param ddayId
   * @param startDate
   * @param termWeeks
   * @param enabled
   */
  @Transactional
  public ToggleAlarmResponseDto toggleAlarm(Long userId, Long ddayId, LocalDate startDate,
      Integer termWeeks, Boolean enabled) {
    Dday dday = ddayRepository.findById(ddayId)
        .orElseThrow(() -> new RuntimeException("D-day를 찾을 수 없습니다."));

    if (!dday.getUser().getId().equals(userId)) {
      throw new RuntimeException("해당 유저가 아니므로 권한이 없습니다");
    }

    if (termWeeks == null || termWeeks < 1) {
      throw new RuntimeException("주기는 1 이상이어야 합니다.");
    }

    dday.setIsAlarm(enabled);
    dday.setDay(startDate);
    dday.setTerm(termWeeks);

    LocalDate nextDate = null;
    LocalDateTime scheduleAt = null;
    boolean created = false;
    int canceled = 0;

    // 알림 스위치 ON/OFF 분기
    if (Boolean.TRUE.equals(enabled)) {
      /**
       * [ON] 켜진 경우 → "다음 발생일" 1건을 예약 큐에 넣는다.
       *     - startDate: 반복의 기준 시작일(처음 D-Day)
       *     - termWeeks: 주기(몇 주마다 반복인지)
       *     - today 는 시스템 오늘(KST 기준)을 넘긴다.
       */
      LocalDate computedNext = nextOccurrence(startDate, termWeeks, LocalDate.now(KST));

      // 실제 발송 예정 시각을 "다음 발생일 + 지정된 시각(예: 09:00)"으로 만든다.
      // - 날짜(LocalDate)와 시각(LocalTime)을 합쳐 LocalDateTime 생성
      LocalDateTime computedScheduleAt = LocalDateTime.of(computedNext,
          LocalTime.of(SEND_HOUR, SEND_MINUTE));

      // 예약 중복 생성을 막기 위한 유니크 키를 만든다 (userId | ddayId | nextDate)
      // - 같은 사용자(userId) / 같은 D-Day(ddayId) / 같은 목표 날짜(nextDate)의 조합은 1개만 존재해야 한다.
      String uniqueKey = buildUniqueKey(ddayId, userId, computedNext);

      // 이미 같은 예약이 존재하면(동일 uk) 새로 만들지 않는다. (토글 연타/동시성으로 인한 중복 방지)
      if (!reservationRepository.existsByUniqueKey(uniqueKey)) {
        // 예약 엔티티 1건 생성: 상태는 PENDING(발송 대기)
        DdayAlarmReservation resv = DdayAlarmReservation.builder()
            .dday(dday)                     // 어떤 D-Day의 예약인지 (연관관계)
            .userId(userId)                 // 대상 사용자(조회 최적화를 위해 캐시)
            .targetDate(computedNext)           // 사용자에게 보여줄 목표 날짜(예: '사료 주문일' 자체)
            .scheduledAt(computedScheduleAt)        // 실제 발송 예정 시각(예: 2025-02-24 09:00)
            .status(AlarmStatus.PENDING)    // 아직 안 보냄 → 대기 상태
            .uniqueKey(uniqueKey)                  // 중복 방지용 키(userId|ddayId|date)
            .build();

        // 예약 큐에 저장 → 스케줄러가 PENDING + 시간 도래분을 집어가서 발송
        reservationRepository.save(resv);
        created = true;
      }
      nextDate = computedNext;
      scheduleAt = computedScheduleAt;
    } else {
      // [OFF] 꺼진 경우 → "앞으로 보낼 수 있는" 예약들을 취소 처리한다.
      // - 이미 보낸 것(SENT)은 건드릴 필요 없음
      // - 아직 발송되지 않은 것(PENDING)만 찾아서 CANCELED로 바꿔 스케줄러가 건너뛰게 한다.
      List<DdayAlarmReservation> pendings =
          reservationRepository.findByDdayIdAndStatus(ddayId, AlarmStatus.PENDING);
      for (DdayAlarmReservation r : pendings) {
        r.setStatus(AlarmStatus.CANCELED);
      }
      canceled = pendings.size();
    }

    // 응답 DTO로 만들어서 반환
    return ToggleAlarmResponseDto.builder()
        .ddayId(ddayId)
        .userId(userId)
        .enabled(enabled)
        .nextDate(nextDate)
        .scheduledAt(scheduleAt)
        .reservationCreated(created)
        .canceledCount(canceled)
        .build();
  }

  /**
   * 예약 중복 생성을 막기 위한 유니크 키를 만든다. - 포맷: "userId|ddayId|date" - 동일 사용자/동일 D-Day/동일 목표 날짜 조합의 예약은 1번만
   * 존재해야 하므로, 이 문자열을 DB 컬럼(unique)로 두어 중복 저장을 근본적으로 차단한다.
   *
   * @param ddayId
   * @param userId
   * @param nextDate
   * @return
   */
  private String buildUniqueKey(Long ddayId, Long userId, LocalDate nextDate) {
    return userId + "|" + ddayId + "|" + nextDate;
  }

  /**
   * 다음 발생일 계산 - 의미: startDate에서 termWeeks 주기마다 반복된다고 할 때, 'today' 이후(엄밀히는 today 보다 "뒤")에 도달하는 가장
   * 가까운 날짜를 돌려준다.
   * <p>
   * 동작 요점 1) startDate 가 아직 미래라면 그대로 startDate 반환 2) startDate 가 과거거나 오늘이라면, - 주기(termWeeks)에 맞춰
   * startDate를 n회 더한 날짜들 중, - today "다음"에 오는 첫 날짜를 찾는다. (즉, 오늘은 포함하지 않는다)
   * <p>
   * 왜 오늘을 포함하지 않나? - 일반적으로 "오늘 아침 9시에 보내는 예약"은 어제 밤에 미리 만들어져 있어야 한다. - 토글 시점이 오늘이라면, 바로 "다음 회차"부터
   * 켜지는 UX가 자연스럽기 때문. (필요하면 오늘도 포함하도록 로직을 바꿀 수 있음)
   *
   * @param startDate
   * @param termWeeks
   * @param today
   * @return
   */
  private LocalDate nextOccurrence(LocalDate startDate, Integer termWeeks, LocalDate today) {

    /**
     *예시로 보는 nextOccurrence:
     * - startDate = 2025-02-01(토), termWeeks=2(2주), today=2025-02-10(월)
     * days = 9일 → weeksPassed=1주
     * 1*7 % 14 != 0 → stepWeeks = weeksPassed + 1 = 2
     * candidate = 2025-02-01 + 2*2주 = 2025-03-01(토)
     * 2025-03-01은 today(02-10) 이후 → 반환: 2025-03-01
     *
     * - startDate = 2025-02-10(오늘), termWeeks=1, today=2025-02-10
     * startDate 는 today 와 같음 → 위 조건에 의해 '오늘'은 건너뛰고 다음 회차(1주 뒤) 반환
     * candidate = 2025-02-17 → 반환: 2025-02-17
     */

    // 케이스A : 시작일이 아직 '오늘 이후'면, 다음 발생일은 시작일 자체
    if (startDate.isAfter(today)) {
      return startDate;
    }

    // 케이스 B : 시작일이 과거나 오늘일 때 -> 주기에 맞춰 startDate를 앞으로 굴린다.
    // 1) startDate ~ today 간 '날짜 차이'를 일(day) 단위로 계산
    long days = Duration.between(startDate.atStartOfDay(), today.atStartOfDay()).toDays();

    // 2) 지나간 '주(week)'수를 구한다. (소수점 버림)
    long weeksPassed = days / 7;

    // 3) 다음에 더할 '몇 주 단위(stepWeeks)'를 정한다.
    //    - (weeksPassed * 7) % (termWeeks * 7) == 0  → 오늘이 정확히 주기 배수 지점이라는 뜻
    //    - 그런데 startDate == today 인 경우는 '오늘'을 건너뛰고 다음 회차를 잡기 위해 +1을 강제한다.
    long stepWeeks =
        (weeksPassed * 7) % (termWeeks * 7) == 0 && !startDate.isEqual(today)
            ? weeksPassed        // 정확히 배수 지점인데 '오늘'이 아니라면, 지금 지점에서 termWeeks만큼만 더하면 됨
            : weeksPassed + 1;   // 그 외(배수가 아님 or '오늘'과 같음)엔 다음 회차로 한 스텝 더 간다.

    // 4) 후보 날짜: startDate + (stepWeeks * termWeeks) 주
    LocalDate candidate = startDate.plusWeeks(stepWeeks * termWeeks);

    // 5) 안전장치: 혹시라도 candidate가 today와 같거나 이전이라면,
    //    주기만큼 계속 더해 'today'보다 "뒤"가 되는 시점까지 밀어준다.
    //    (일반적으로 위 계산으로 이미 today 이후가 나오지만, 경계값에서 안전하게 한 번 더 보정)
    while (!candidate.isAfter(today)) {
      candidate = candidate.plusWeeks(termWeeks);
    }

    // 6) 결과: today 이후(미래) 중 가장 가까운 발생일
    return candidate;
  }

  // =========== 예약 처리 스케줄러 ======================

  /**
   * 매 1분마다: scheduledAt <= now && PENDING (알림 보낼 시각 + 대기상태 알림) -> 발송 - 성공시 SENT - isAlarm=true 면 다음
   * 회차 예약 1건 자동 생성
   */
  @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
  @Transactional
  public void processDueReservation() {
    LocalDateTime now = LocalDateTime.now(KST);

    // 대기상태에 있는 알림 500개 선에서 조회하기 (발송 준비)
    List<DdayAlarmReservation> due = reservationRepository.findDueReservations(
        AlarmStatus.PENDING, now, PageRequest.of(0, 500));

    for (DdayAlarmReservation reservation : due) {
      Dday dday = reservation.getDday(); // 예약이 가리키는 D-Day
      User user = dday.getUser(); // D-Day 소유 유저

      long daysRemaining = ChronoUnit.DAYS.between(LocalDate.now(KST), reservation.getTargetDate());
      String body = (daysRemaining == 0)
          ? dday.getTitle() + "디데이입니다"
          : dday.getTitle() + "까지 " + daysRemaining + "일 남았습니다.";

      try {
        // 알림 푸시 + 이력 저장을 단일 서비스에 위임 (중복 발송 방지)
        notificationService.sendNotificationAndSave(NotificationType.DDAY, user, body);

        // 발송 성공 → 예약은 SENT
        reservation.setStatus(AlarmStatus.SENT);

        // 다음 회차 예약 자동 생성 (알림 유지 + 주기 존재 시)
        if (Boolean.TRUE.equals(dday.getIsAlarm()) && dday.getTerm() != null
            && dday.getTerm() > 0) {
          dday.setDay(dday.getDay().plusWeeks(dday.getTerm())); // 주기를 더해서 Day 다시 세팅

          LocalDate nextDate = nextOccurrence(dday.getDay(), dday.getTerm(), LocalDate.now(KST));
          LocalDateTime nextAt = nextDate.atTime(SEND_HOUR, SEND_MINUTE);
          String uniqueKey = buildUniqueKey(dday.getId(), user.getId(), nextDate);

          // 이미 유니크 키가 있는지 확인 -> 중복 검사
          if (!reservationRepository.existsByUniqueKey(uniqueKey)) {
            DdayAlarmReservation nextReservation = DdayAlarmReservation.builder()
                .dday(dday)
                .userId(user.getId())
                .targetDate(nextDate)
                .scheduledAt(nextAt)
                .status(AlarmStatus.PENDING)
                .uniqueKey(uniqueKey)
                .build();
            reservationRepository.save(nextReservation);
          }
        } //// if (Boolean.TRUE.equals(dday.getIsAlarm()) && dday.getTerm() != null && dday.getTerm() > 0)
      } catch (Exception e) {
        // 실패 시 상태는 PENDING 유지 → 다음 사이클에서 재시도
        log.error("예약 발송 실패 ddayId={}, userId={}, error={}",
            dday.getId(), user.getId(), e.getMessage(), e);
      }
    } //// for (DdayAlarmReservation reservation : due)
  }


}