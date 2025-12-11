package DC_square.spring.web.dto.response.place;

import DC_square.spring.domain.enums.PlaceCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class HotPlaceResponseDTO {

  private Long id;
  private String name;
  private PlaceCategory category;
  private Double distance;
  private String imgUrl;
}
