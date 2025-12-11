package DC_square.spring.web.dto.request.place;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceRequestDTO {

  private Double longitude;
  private Double latitude;
  private String keyword;
}
