package DC_square.spring.web.dto.request.user;

import DC_square.spring.domain.enums.DogCat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PetRegistrationDto {

  @Size(max = 20, message = "반려동물 이름은 20자 이하여야 합니다.")
  private String petName;

  @NotNull(message = "강아지/고양이 구분은 필수입니다.")
  private DogCat dogCat;

  @NotBlank(message = "품종은 필수입니다.")
  @Size(max = 20, message = "품종은 20자 이하여야 합니다.")
  @Schema(example = "품종")
  private String breed;

  @NotBlank(message = "생년월일은 필수입니다.")
  @Schema(example = "2024-01-12")
  private String birth; // "yyyy. MM. dd" 또는 "yyyy.MM.dd" 형식

  // birth String을 LocalDate로 변환
  public LocalDate convertBirthToLocalDate() {
    try {
      // "yyyy. MM. dd" 형식 시도
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");
      return LocalDate.parse(this.birth, formatter);
    } catch (DateTimeParseException e1) {
      try {
        // "yyyy.MM.dd" 형식 시도
        String formattedDate = this.birth.replace(".", "-");
        return LocalDate.parse(formattedDate);
      } catch (DateTimeParseException e2) {
        throw new IllegalArgumentException("날짜 형식이 올바르지 않습니다. (예: 2024. 01. 01 또는 2024.01.01)");
      }
    }
  }
}