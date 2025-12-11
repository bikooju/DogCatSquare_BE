package DC_square.spring.web.dto.response.walk;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class WalkSearchResponseDto {

  private int status;
  private boolean success;
  private String message;
  private List<WalkResponseDto.WalkDto> walks;
}
