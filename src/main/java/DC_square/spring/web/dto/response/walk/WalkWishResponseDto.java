package DC_square.spring.web.dto.response.walk;

import lombok.Builder;

@Builder
public class WalkWishResponseDto {

  private int status;
  private boolean success;
  private String message;
}
