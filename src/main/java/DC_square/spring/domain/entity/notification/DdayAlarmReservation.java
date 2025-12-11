package DC_square.spring.domain.entity.notification;

import DC_square.spring.domain.entity.Dday;
import DC_square.spring.domain.enums.AlarmStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * D-Day 알람 예약 엔티티 (작업 큐) - 한 레코드 = 특정 사용자/디데이/날짜에 대해 '정해진 시각(scheduledAt)'에 보낼 1건의 작업 -
 * AlarmStatus의 PENDING -> SENT/CREATED 로 관리 (중복/재시도 제어)
 */
@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "dday_alarm_reservation",
    indexes = {
        @Index(name = "idx_dday_alarm_due_status", columnList = "status,scheduledAt"), // 만기 조회용
        @Index(name = "idx_dday_alarm_dday", columnList = "dday_id")                    // 조인 최적화
    })
public class DdayAlarmReservation {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  // 어떤 D-DAY 예약인지 (지연로딩)
  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "dday_id", nullable = false)
  private Dday dday;

  // 사용자 ID 캐시(조인 최소화 목적)
  @Column(nullable = false)
  private Long userId;

  // 사용자에게 보여줄 목표 날짜(예: 2025-02-24)
  @Column(nullable = false)
  private LocalDate targetDate;

  // 실제 발송 예정 시각(예: 2025-02-24T09:00)
  @Column(nullable = false)
  private LocalDateTime scheduledAt;

  // 예약 상태(PENDING / SENT / CANCELED)
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private AlarmStatus status;

  @CreationTimestamp
  private LocalDateTime createdAt;

  // 중복 예약 방지용 유니크 키 (userId|ddayId|targetDate)
  @Column(unique = true, length = 64, nullable = false)
  private String uniqueKey;
}
