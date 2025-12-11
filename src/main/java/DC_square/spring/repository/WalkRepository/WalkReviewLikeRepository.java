package DC_square.spring.repository.WalkRepository;

import DC_square.spring.domain.entity.walk.WalkReviewLike;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalkReviewLikeRepository extends JpaRepository<WalkReviewLike, Long> {

  boolean existsByUserIdAndWalkReviewId(Long userId, Long walkReviewId);

  List<WalkReviewLike> findByUserId(Long userId);
}