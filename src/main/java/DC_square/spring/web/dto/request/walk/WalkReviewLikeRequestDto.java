package DC_square.spring.web.dto.request.walk;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WalkReviewLikeRequestDto {

  private Long userId;

  private Boolean isLiked;
}
