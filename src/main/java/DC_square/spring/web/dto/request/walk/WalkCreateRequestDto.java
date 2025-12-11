package DC_square.spring.web.dto.request.walk;

import DC_square.spring.domain.entity.Coordinate;
import DC_square.spring.domain.enums.Difficulty;
import DC_square.spring.web.dto.response.walk.WalkResponseDto;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WalkCreateRequestDto {

  private String title;
  private String description;
  private Integer time;
  private Double distance;
  private Difficulty difficulty;
  private List<WalkResponseDto.SpecialDto> special;
  private List<Coordinate> coordinates;
}