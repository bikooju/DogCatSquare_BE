package DC_square.spring.web.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRequestDto {

  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
  private String password;

  @NotBlank(message = "닉네임은 필수입니다.")
  @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
  private String nickname;

  @NotBlank(message = "전화번호는 필수입니다.")
  @Pattern(regexp = "^\\d{11}$", message = "전화번호는 11자리 숫자여야 합니다.")
  private String phoneNumber;

  private String regionId;
}