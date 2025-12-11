package DC_square.spring.repository;

import DC_square.spring.domain.entity.Region;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RegionRepository extends JpaRepository<Region, Long> {

  // 시/도/구로 Region 찾기
  Optional<Region> findByDoNameAndSiAndGu(String doName, String si,
      String gu);  // findByDoAndSiAndGu 를 findByDoNameAndSiAndGu 로 변경
}
