package DC_square.spring.web.dto.response.walk;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalkDetailResponseDto {

  private Long walkId;
  private String title;
  private String description;
  private List<String> walkImageUrl;
  private Integer time;
  private Double distance;
  private String difficulty;
  private List<WalkResponseDto.SpecialDto> special;
  private List<WalkResponseDto.CoordinateDto> startCoordinate;
  private List<WalkResponseDto.CoordinateDto> endCoordinate;
  private String startAddress;
  private String endAddress;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private WalkResponseDto.CreatedByDto createdBy;
}