package DC_square.spring.web.dto.response.walk;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class WalkResponseDto {

  private List<WalkDto> walks;

  @Getter
  @Builder
  public static class WalkDto {

    private Long walkId;
    private String title;
    private String description;
    private List<String> walkImageUrl;
    private Integer reviewCount;
    private Double distance;
    private Integer time;
    private String difficulty;
    private List<SpecialDto> special;
    private List<CoordinateDto> coordinates;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private CreatedByDto createdBy;
  }

  @Getter
  @Builder
  public static class SpecialDto {

    private String type;
    private String customValue;
  }

  @Getter
  @Builder
  public static class CoordinateDto {

    private Double latitude;
    private Double longitude;
    private Integer sequence;
  }

  @Getter
  @Builder
  public static class CreatedByDto {

    private String nickname;
    private String breed;
    private String profileImageUrl;
  }
}