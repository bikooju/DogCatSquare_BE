package DC_square.spring.web.controller.notification;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.domain.enums.NotificationType;
import DC_square.spring.service.dday.DdayService;
import DC_square.spring.service.notification.NotificationService;
import DC_square.spring.util.UserUtil;
import DC_square.spring.web.dto.request.notification.ReadNotificationRequestDto;
import DC_square.spring.web.dto.response.notification.NotificationInfoDto;
import DC_square.spring.web.dto.response.notification.ToggleAlarmResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/notification")
@Tag(name = "Notification", description = "알림 관련 API")
public class NotificationController {

  private final DdayService ddayService;
  private final NotificationService notificationService;
  private final UserUtil userUtil;

  public static class ToggleRequest {

    @NotNull
    public LocalDate startDate;
    @NotNull
    @Min(1)
    public Integer termWeeks;
    @NotNull
    public Boolean enabled;
  }

  @Operation(summary = "반려동물 알림 토글 API", description = "startDate: 시작날짜 "
      + "\n\ntermWeeks: 주기"
      + "\n\nenabled: 주기 알람 받기 활성화/비활성화")
  @PostMapping("/dday/{ddayId}/toggle")
  public ApiResponse<ToggleAlarmResponseDto> toggleAlarm(
      @PathVariable Long ddayId,
      @RequestParam Long userId,
      @RequestBody ToggleRequest request
  ) {
    ToggleAlarmResponseDto response = ddayService.toggleAlarm(userId, ddayId, request.startDate,
        request.termWeeks, request.enabled);
    return ApiResponse.onSuccess(response, "알림 설정이 변경되었습니다.");
  }

  @Operation(summary = "클라이언트와 서버 SSE 연결해주는 sse 연결 구독(이거 제일 먼저 하기)", description = "토큰 필수, sse연결시 이거 제일 먼저 요청해서 연결 해놔야함")
  @GetMapping(value = "/sse/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
  public SseEmitter subscribe(HttpServletRequest request) {
    Long currentUserId = userUtil.getCurrentUserId(request);
    return notificationService.subscribe(currentUserId);
  }

  @Operation(summary = "(실제사용X)테스트용 sse로 메시지(내용) 보내기", description = "테스트용으로 알림 메시지 보내기")
  @PostMapping("/sse/publish")
  public void publish(HttpServletRequest request, @RequestParam String content) {
    Long currentUserId = userUtil.getCurrentUserId(request);
    notificationService.createAndSend(currentUserId, NotificationType.TEST, content);
  }

  @Operation(summary = "알림 읽음 처리(단건/여러건)", description =
      "ids : 읽음 처리할 알림 id들(단건도 가능하고 요청 비어있거나 null이면 전부 읽음 처리)" +
          "\nreadAt : 알림 읽은시간(요청 비어있으면 현재시간으로 처리)")
  @PostMapping("/read")
  public void readNotifications(HttpServletRequest request,
      @Valid @RequestBody ReadNotificationRequestDto requestDto) {
    Long currentUserId = userUtil.getCurrentUserId(request);
    notificationService.readNotifications(currentUserId, requestDto);
  }

  @Operation(summary = "(실제사용X)테스트용으로 읽지 않은 알림들 조회", description = "단순 읽지 않은 알림들 조회 기능")
  @GetMapping("/test-read")
  public ResponseEntity<List<NotificationInfoDto>> getUnReadNotifications(
      HttpServletRequest request) {
    Long currentUserId = userUtil.getCurrentUserId(request);
    List<NotificationInfoDto> unreadNotifications = notificationService.getUnreadNotifications(
        currentUserId);
    return ResponseEntity.ok(unreadNotifications);
  }


}
