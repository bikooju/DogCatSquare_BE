package DC_square.spring.web.dto.response.place;

import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder

public class PlaceDetailResponseDTO {

  private Long id;
  private String name;
  private String address;
  private String category;
  private String phoneNumber;
  private Boolean open;
  private Double longitude;
  private Double latitude;
  private String businessHours;
  private String homepageUrl;
  private String description;
  private List<String> facilities;
  private List<String> imageUrls;
  private Double distance; //구글 API 반환값을 위해 추가
  private boolean isWished;
  private Integer reviewCount;
  private List<PlaceReviewResponseDTO> recentReviews;
  private List<String> keywords;
  private String additionalInfo;
}
