package DC_square.spring.web.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerificationResponseDto {

  private String message;
  private boolean verified;
}