package DC_square.spring.repository.place;

import DC_square.spring.domain.entity.place.Place;
import DC_square.spring.domain.entity.place.PlaceDetail;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PlaceDetailRepository extends JpaRepository<PlaceDetail, Long> {

  Optional<PlaceDetail> findByPlace(Place place);

  Optional<PlaceDetail> findByPlaceId(Long placeId);
}
