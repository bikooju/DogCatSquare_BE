package DC_square.spring.web.dto.request.user;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PetsAddRequestDto {

  @NotEmpty(message = "최소 한 마리의 반려동물 정보가 필요합니다.")
  @Valid
  private PetRegistrationDto pet;
}