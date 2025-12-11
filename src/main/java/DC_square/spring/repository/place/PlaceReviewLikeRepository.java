package DC_square.spring.repository.place;

import DC_square.spring.domain.entity.place.PlaceReviewLike;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceReviewLikeRepository extends JpaRepository<PlaceReviewLike, Long> {

  boolean existsByUserIdAndPlaceReviewId(Long userId, Long placeReviewId);
}
