package DC_square.spring.web.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EmailVerifyRequestDto {

  private String email;
  private String verificationCode;
}