package DC_square.spring.repository.WalkRepository;

import DC_square.spring.domain.entity.walk.Walk;
import DC_square.spring.domain.entity.walk.WalkReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface WalkReviewRepository extends JpaRepository<WalkReview, Long> {

  List<WalkReview> findAllByWalkId(Long walkId);

  List<WalkReview> findByWalk(Walk walk);

  int countByWalk(Walk walk);

  @Query("SELECT wr FROM WalkReview wr WHERE wr.user.id = :userId")
  List<WalkReview> findAllByUserId(@Param("userId") Long userId);
}
