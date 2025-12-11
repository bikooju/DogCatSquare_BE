package DC_square.spring.repository.WalkRepository;

import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.walk.Walk;
import DC_square.spring.domain.entity.walk.WalkWish;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WalkWishRepository extends JpaRepository<WalkWish, Long> {

  boolean existsByUserAndWalk(User user, Walk walk);

  List<WalkWish> findByUserAndIsWished(User user, boolean isWished);

  Optional<WalkWish> findByUserAndWalk(User user, Walk walk);
}
