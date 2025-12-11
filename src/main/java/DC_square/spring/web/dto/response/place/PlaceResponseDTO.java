package DC_square.spring.web.dto.response.place;

import DC_square.spring.domain.enums.PlaceCategory;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class PlaceResponseDTO {

  private Long id;
  private String name;
  private String address;
  private PlaceCategory category;
  private String phoneNumber;
  private Double longitude;
  private Double latitude;
  private Double distance;
  private Boolean open;
  private String imgUrl;
  private Integer reviewCount;
  private List<String> keywords;
}
