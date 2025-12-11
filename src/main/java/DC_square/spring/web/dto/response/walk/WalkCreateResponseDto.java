package DC_square.spring.web.dto.response.walk;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WalkCreateResponseDto {

  private boolean success;
  private String message;
  private long walkId;
}