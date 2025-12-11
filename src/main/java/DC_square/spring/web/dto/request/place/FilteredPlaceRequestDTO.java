package DC_square.spring.web.dto.request.place;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class FilteredPlaceRequestDTO {

  private LocationRequestDTO location;
  private Boolean is24Hours;
  private Boolean hasParking;
  private Boolean isCurrentlyOpen;
}
