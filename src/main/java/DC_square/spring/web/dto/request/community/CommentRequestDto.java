package DC_square.spring.web.dto.request.community;

import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class CommentRequestDto {

  @NotBlank(message = "댓글 내용은 필수입니다.")
  private String content;

  private Long parentId; //대댓글인 경우 부모 댓글 ID
}
