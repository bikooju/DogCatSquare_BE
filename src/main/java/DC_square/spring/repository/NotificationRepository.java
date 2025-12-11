package DC_square.spring.repository;

import DC_square.spring.domain.entity.notification.UserNotification;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface NotificationRepository extends JpaRepository<UserNotification, Long> {

  // 특정 유저의 읽지 않은 알림 목록 조회
  List<UserNotification> findByUserIdAndIsReadFalseOrderByCreatedAtDesc(Long userId);

  // 특정 사용자 알림 전체 "읽음" 처리 (현재 읽지 않은 것만)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
      UPDATE UserNotification n
         SET n.isRead = true
       WHERE n.user.id = :userId
         AND n.isRead = false
      """)
  int bulkMarkReadAll(@Param("userId") Long userId);

  // 특정 ID들만 "읽음" 처리 (현재 읽지 않은 것만)
  @Modifying(flushAutomatically = true, clearAutomatically = true)
  @Query("""
      UPDATE UserNotification n
         SET n.isRead = true
       WHERE n.user.id = :userId
         AND n.id IN :ids
         AND n.isRead = false
      """)
  int bulkMarkReadByIds(@Param("userId") Long userId,
      @Param("ids") List<Long> ids);

}