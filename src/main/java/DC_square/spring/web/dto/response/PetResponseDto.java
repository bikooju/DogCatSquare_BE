package DC_square.spring.web.dto.response;

import DC_square.spring.domain.entity.Pet;
import DC_square.spring.domain.enums.DogCat;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class PetResponseDto {

  private Long id;
  private String petName;
  private DogCat dogCat;
  ;
  private String breed;
  private String birth;
  private String petImageUrl;

  public static PetResponseDto from(Pet pet) {
    return PetResponseDto.builder()
        .id(pet.getId())
        .petName(pet.getPetName())
        .dogCat(pet.getDogCat())
        .breed(pet.getBreed())
        .birth(pet.getBirth().format(DateTimeFormatter.ofPattern("yyyy. MM. dd")))
        .petImageUrl(pet.getPetImageUrl())
        .build();

  }
}
