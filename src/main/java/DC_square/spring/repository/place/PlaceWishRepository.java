package DC_square.spring.repository.place;

import DC_square.spring.domain.entity.place.PlaceWish;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceWishRepository extends JpaRepository<PlaceWish, Long> {

  boolean existsByPlaceIdAndUserId(Long placeId, Long userId);

  Optional<PlaceWish> findByUserIdAndPlaceId(Long userId, Long placeId);

  List<PlaceWish> findAllByUserId(Long userId);
}
