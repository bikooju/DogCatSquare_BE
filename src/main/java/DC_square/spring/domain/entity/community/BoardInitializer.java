package DC_square.spring.domain.entity.community;

import DC_square.spring.domain.enums.BoardType;
import DC_square.spring.repository.community.BoardRepository;
import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class BoardInitializer {

  private final BoardRepository boardRepository;

  @PostConstruct
  public void initializeBoards() {
    for (BoardType type : BoardType.values()) {
      //이미 존재하는지 확인
      boolean exists = boardRepository.existsByBoardType(type);

      // 존재하지 않으면 생성
      if (!exists) {
        Board board = Board.builder()
            .boardType(type)
            .content(type.getDisplayName() + "입니다.") // 기본 내용
            .createdDate(LocalDateTime.now()).
            build();
        boardRepository.save(board);
      }
    }
  }

}
