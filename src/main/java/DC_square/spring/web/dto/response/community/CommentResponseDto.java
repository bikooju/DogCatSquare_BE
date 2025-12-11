package DC_square.spring.web.dto.response.community;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentResponseDto {

  private Long id;
  private Long userId;
  private String content;
  private String name;
  private String animal_type;
  private String profileImage_URL;
  private LocalDateTime created_at;
  private List<CommentResponseDto> replies;
}
