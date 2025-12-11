package DC_square.spring.web.dto.response.notification;

import DC_square.spring.domain.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 실제 푸시 발송 결과 응답 DTO - fcmAttempted: 토큰이 있을 때 FCM 전송 시도했는지 - fcmSuccess: FCM SDK가 성공 응답을 줬는지 -
 * fcmResponseId: FCM의 messageId(성공 시) - notificationId: DB에 저장된 알림 이력(사용자 알림함) PK
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationDeliveryResponseDto {

  private Long userId;
  private NotificationType notificationType;
  private String content;

  private boolean fcmAttempted;
  private boolean fcmSuccess;
  private String fcmResponseId;          // 성공 시 FCM messageId
  private String errorMessage;           // 실패 시 이유(있으면)

  private Long notificationId;           // 저장된 알림 이력 PK
  private LocalDateTime createdAt;       // 저장 시각
  private boolean isRead; // 읽었는지
}
