package DC_square.spring.repository.WalkRepository;

import DC_square.spring.domain.entity.walk.Walk;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface WalkRepository extends JpaRepository<Walk, Long> {

  @Query("SELECT w FROM Walk w JOIN w.coordinates c WHERE ST_Distance_Sphere(point(c.longitude, c.latitude), point(:longitude, :latitude)) < :radius")
  List<Walk> findNearbyWalks(@Param("latitude") Double latitude,
      @Param("longitude") Double longitude,
      @Param("radius") Double radius);

  boolean existsByTitle(String title);

  @Query("SELECT w FROM Walk w WHERE w.id = :walkId")
  Optional<Walk> findById(@Param("walkId") Long walkId);

  List<Walk> findByTitleContaining(String title);

  List<Walk> findByCreatedById(Long userId);

}
