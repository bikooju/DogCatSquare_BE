package DC_square.spring.repository.region;

import DC_square.spring.domain.entity.region.Province;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProvinceRepository extends JpaRepository<Province, Long> {

  Optional<Province> findByName(String name);
}
