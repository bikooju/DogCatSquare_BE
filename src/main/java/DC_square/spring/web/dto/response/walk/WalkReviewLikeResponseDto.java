package DC_square.spring.web.dto.response.walk;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Builder
@Setter
public class WalkReviewLikeResponseDto {

  private int status;
  private boolean success;
  private String message;
}
