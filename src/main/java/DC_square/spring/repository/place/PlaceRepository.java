package DC_square.spring.repository.place;

import DC_square.spring.domain.entity.place.Place;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {

  Optional<Place> findByGooglePlaceId(String googlePlaceId);

  boolean existsByGooglePlaceId(String googlePlaceId);

  @Query("SELECT p, COUNT(w) as wishCount " +
      "FROM Place p " +
      "LEFT JOIN PlaceWish w ON p.id = w.place.id " +
      "WHERE p.city.id = :cityId " +
      "GROUP BY p " +
      "ORDER BY COUNT(w) DESC")
  List<Object[]> findAllByCityIdOrderByWishCount(@Param("cityId") Long cityId, Pageable pageable);
}