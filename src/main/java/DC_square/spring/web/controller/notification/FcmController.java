package DC_square.spring.web.controller.notification;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.service.notification.NotificationService;
import DC_square.spring.web.dto.request.notification.FcmTokenRequestDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fcm")
@Tag(name = "FCM Token", description = "FCM 토큰 설정 API (알림 테스트 하기 전에 꼭 먼저 하기)")
public class FcmController {

  private final NotificationService notificationService;

  @Operation(summary = "FCM 토큰 등록 API", description = "userId와 Firebase Cloud Messaging 토큰 입력")
  @PostMapping("/register/token")
  public ApiResponse<Void> registerToken(@RequestBody FcmTokenRequestDto dto) {
    notificationService.registerFcmToken(dto.getUserId(), dto.getFcmToken());
    return ApiResponse.onSuccess(null, "FCM 토큰이 등록되었습니다.");
  }
}