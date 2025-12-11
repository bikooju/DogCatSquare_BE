package DC_square.spring.web.dto.response.notification;


import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * D-DAY 알림 토글 결과 응답 DTO - enabled: 최종 ON/OFF 상태 - nextDate/scheduledAt: ON인 경우 다음 예약 정보(없으면 null) -
 * reservationCreated: 이번 호출에서 신규 예약을 만들었는지 - canceledCount: OFF 시 취소(상태 변경)된 예약 개수
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToggleAlarmResponseDto {

  private Long ddayId;
  private Long userId;
  private Boolean enabled;

  private LocalDate nextDate;               // ON일 때만 세팅(없으면 null)
  private LocalDateTime scheduledAt;        // ON일 때만 세팅(없으면 null)

  private boolean reservationCreated;       // 이번 호출에서 새 예약 생성했는지
  private int canceledCount;                // OFF면 취소된 예약 수, ON이면 0
}
