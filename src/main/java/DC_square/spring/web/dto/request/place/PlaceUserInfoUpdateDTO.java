package DC_square.spring.web.dto.request.place;

import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceUserInfoUpdateDTO {

  private List<String> keywords;
  private String additionalInfo;
}
