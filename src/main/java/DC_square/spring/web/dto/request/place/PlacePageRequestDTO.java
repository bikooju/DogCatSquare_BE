package DC_square.spring.web.dto.request.place;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class PlacePageRequestDTO {

  private int page = 0;
  private final int size = 10;
}
