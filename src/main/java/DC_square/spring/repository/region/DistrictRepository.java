package DC_square.spring.repository.region;

import DC_square.spring.domain.entity.region.City;
import DC_square.spring.domain.entity.region.District;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DistrictRepository extends JpaRepository<District, Long> {

  Optional<District> findByNameAndCity(String name, City city);
}