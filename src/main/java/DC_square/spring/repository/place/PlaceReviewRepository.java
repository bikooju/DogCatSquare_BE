package DC_square.spring.repository.place;

import DC_square.spring.domain.entity.place.PlaceReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceReviewRepository extends JpaRepository<PlaceReview, Long> {

  List<PlaceReview> findAllByPlaceId(Long placeId);

  @Query("SELECT pr FROM PlaceReview pr WHERE pr.user.id = :userId")
  List<PlaceReview> findAllByUserId(@Param("userId") Long userId);

  @Query("SELECT COUNT(pr) FROM PlaceReview pr WHERE pr.place.id = :placeId")
  int countByPlaceId(@Param("placeId") Long placeId);

  // 최근 리뷰 2개 조회
  @Query("SELECT pr FROM PlaceReview pr WHERE pr.place.id = :placeId ORDER BY pr.createdAt DESC LIMIT 2")
  List<PlaceReview> findTop2ByPlaceOrderByCreatedAtDesc(@Param("placeId") Long placeId);
}
