package DC_square.spring.repository.region;

import DC_square.spring.domain.entity.region.City;
import DC_square.spring.domain.entity.region.Province;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CityRepository extends JpaRepository<City, Long> {

  Optional<City> findByNameAndProvince(String name, Province province);
}