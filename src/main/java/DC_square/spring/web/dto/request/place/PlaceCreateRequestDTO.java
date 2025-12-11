package DC_square.spring.web.dto.request.place;

import DC_square.spring.domain.enums.PlaceCategory;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceCreateRequestDTO {

  private String name;
  private String address;
  private PlaceCategory category;
  private String phoneNumber;
  private Boolean open;
  private Double longitude;
  private Double latitude;
  private String businessHours;
  private String homepageUrl;
  private String description;
  //private List<String> facilities;
  private List<String> keywords;
  private String additionalInfo;
}
