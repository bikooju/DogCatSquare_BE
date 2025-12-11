package DC_square.spring.web.dto.request.notification;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class FcmTokenRequestDto {

  @Schema(description = "유저ID")
  private Long userId;

  @Schema(description = "Firebase 클라우드 메시징 토큰")
  private String fcmToken;
}


