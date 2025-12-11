package DC_square.spring.web.dto.request.notification;

import DC_square.spring.domain.enums.NotificationType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FcmMessageRequestDto {

  @Schema(description = "유저ID")
  private Long id;

  @Schema(description = "알림 타입 (COMMENT, DDAY)")
  private NotificationType notificationType;

  @Schema(description = "메시지 제목")
  private String title;

  @Schema(description = "메시지 내용")
  private String content;

  @Schema(description = "FCM 토큰")
  private String fcmToken;
}
