package DC_square.spring.web.dto.response.community;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyBoardResponseDto {

  private Long id;
  private Long boardId;
  private String username;
  private String boardName;
}
