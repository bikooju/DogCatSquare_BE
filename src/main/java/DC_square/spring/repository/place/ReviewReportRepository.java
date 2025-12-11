package DC_square.spring.repository.place;

import DC_square.spring.domain.entity.place.ReviewReport;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReviewReportRepository extends JpaRepository<ReviewReport, Long> {

  // 특정 유저가 신고한 리뷰 ID 목록 조회
  @Query("SELECT r.review.id FROM ReviewReport r WHERE r.reporter.id = :userId")
  List<Long> findReportedReviewIdsByUserId(@Param("userId") Long userId);

  // 특정 리뷰에 대한 신고 횟수 카운트
  @Query("SELECT COUNT(r) FROM ReviewReport r WHERE r.review.id = :reviewId")
  int countByReviewId(@Param("reviewId") Long reviewId);

  // 특정 유저가 받은 신고 횟수 카운트
  @Query("SELECT COUNT(r) FROM ReviewReport r WHERE r.reportedUser.id = :userId")
  int countByReportedUserId(@Param("userId") Long userId);

  // 유저가 특정 리뷰를 이미 신고했는지 확인
  boolean existsByReporterIdAndReviewId(Long reporterId, Long reviewId);

  // 특정 유저가 신고당한 횟수가 4회 이상인지 확인
  @Query("SELECT COUNT(DISTINCT r.reportedUser.id) > 0 FROM ReviewReport r " +
      "WHERE r.reportedUser.id = :userId " +
      "GROUP BY r.reportedUser.id " +
      "HAVING COUNT(r) >= 4")
  boolean isUserReportedFourOrMoreTimes(@Param("userId") Long userId);

  // 신고가 4회 이상 접수된 유저 ID 목록 조회
  @Query("SELECT r.reportedUser.id FROM ReviewReport r " +
      "GROUP BY r.reportedUser.id " +
      "HAVING COUNT(r) >= 4")
  List<Long> findUserIdsReportedFourOrMoreTimes();
}
