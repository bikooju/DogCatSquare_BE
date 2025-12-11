package DC_square.spring.web.dto.response.community;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostResponseDto {

  private Long id;
  private String boardType;
  private String username;
  private String animal_type;
  private String title;
  private String content;
  private String video_URL;
  private String thumbnail_URL;
  private String profileImage_URL;
  private List<String> images;
  private Integer like_count;
  private Integer comment_count;
  private LocalDateTime createdAt;
  private LocalDateTime updatedAt;
  private Long userId;
}
