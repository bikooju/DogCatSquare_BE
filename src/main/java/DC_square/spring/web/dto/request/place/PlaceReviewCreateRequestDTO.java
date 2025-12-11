package DC_square.spring.web.dto.request.place;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceReviewCreateRequestDTO {

  private String content;
  private List<String> placeReviewImageUrl;
  private LocalDateTime createdAt;
}
