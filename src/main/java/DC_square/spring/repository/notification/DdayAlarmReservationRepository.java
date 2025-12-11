package DC_square.spring.repository.notification;

import DC_square.spring.domain.entity.notification.DdayAlarmReservation;
import DC_square.spring.domain.enums.AlarmStatus;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * 알림 예약 조회, 중복체크, 상태 조회용 Repository
 */
@Repository
public interface DdayAlarmReservationRepository extends JpaRepository<DdayAlarmReservation, Long> {

  /**
   * 만기된 예약(PENDING && scheduledAt <= now)을 최대 500건까지 조회 - 만기 : 알림을 보낼 수 있는 시각이 됐다 - @Query 로 메서드명
   * 단순화 - pageable로 상한(예: 500) 제어
   */
  @Query("SELECT r FROM DdayAlarmReservation r " +
      "WHERE r.status = :status AND r.scheduledAt <= :now " +
      "ORDER BY r.scheduledAt ASC")
  List<DdayAlarmReservation> findDueReservations(
      @Param("status") AlarmStatus status,
      @Param("now") LocalDateTime now,
      Pageable pageable
  );

  // uniqueKey 중복 여부 확인
  boolean existsByUniqueKey(String uniqueKey);

  // 특정 D-Day의 PENDING 예약 목록 (OFF 시 일괄 취소용)
  List<DdayAlarmReservation> findByDdayIdAndStatus(Long ddayId, AlarmStatus status);


}
