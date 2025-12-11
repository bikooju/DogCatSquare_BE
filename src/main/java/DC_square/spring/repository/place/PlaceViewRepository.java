package DC_square.spring.repository.place;

import DC_square.spring.domain.entity.place.PlaceView;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PlaceViewRepository extends JpaRepository<PlaceView, Long> {

  @Query("SELECT p.place.id, COUNT(p) as viewCount " +
      "FROM PlaceView p " +
      "WHERE p.viewedAt >= :startDate " +
      "AND p.place.city.id = :cityId " +
      "GROUP BY p.place.id " +
      "ORDER BY viewCount DESC")
  List<Object[]> findTopPlacesByViewCount(
      @Param("startDate") LocalDateTime startDate,
      @Param("cityId") Long cityId
  );
}

