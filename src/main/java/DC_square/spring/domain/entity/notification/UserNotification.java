package DC_square.spring.domain.entity.notification;

import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * 실제로 발송된 알림 이력을 저장하는 엔티티 - DdayAlarmReservation는 앞으로 보낼 예정인 알림을 관리
 */
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserNotification {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user; // 어떤 유저에게 보낸 알림인지

  @Enumerated(EnumType.STRING)
  private NotificationType notificationType;

  @Column(name = "content", nullable = false)
  private String content;

  private LocalDateTime createdAt;

  private boolean isRead;

  @PrePersist
  public void prePersist() {
    createdAt = LocalDateTime.now();
  }
}
