package DC_square.spring.web.dto.request.walk;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class WalkRequestDto {

  private Double latitude;
  private Double longitude;
  private Double radius;

  public WalkRequestDto(Double latitude, Double longitude, Double radius) {
    this.latitude = latitude;
    this.longitude = longitude;
    this.radius = (radius == null || radius == 0) ? 1000000.0 : radius;
  }
}