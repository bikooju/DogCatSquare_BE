package DC_square.spring.web.dto.response.notification;

import DC_square.spring.domain.entity.notification.UserNotification;
import DC_square.spring.domain.enums.NotificationType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * sse 알림 dto(FCM 정보 포함 X) - FCM 정보 포함은 NotificationDeliveryResponseDto에서 줌
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationInfoDto {

  private Long id;
  private Long userId;
  private NotificationType type;
  private String content;
  private LocalDateTime createdAt;
  private boolean isRead;

  public static NotificationInfoDto of(UserNotification notification) {
    return NotificationInfoDto.builder()
        .id(notification.getId())
        .userId(notification.getUser().getId())
        .type(notification.getNotificationType())
        .content(notification.getContent())
        .createdAt(notification.getCreatedAt())
        .isRead(notification.isRead())
        .build();
  }
}
