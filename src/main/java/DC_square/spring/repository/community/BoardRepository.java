package DC_square.spring.repository.community;

import DC_square.spring.domain.entity.community.Board;
import DC_square.spring.domain.enums.BoardType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BoardRepository extends JpaRepository<Board, Long> {


  Board findByBoardType(BoardType boardType);

  boolean existsByBoardType(BoardType boardType);
}
