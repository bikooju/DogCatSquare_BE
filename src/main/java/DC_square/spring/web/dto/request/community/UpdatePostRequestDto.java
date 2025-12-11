package DC_square.spring.web.dto.request.community;

import DC_square.spring.domain.enums.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class UpdatePostRequestDto {

  @NotNull(message = "게시판 타입은 필수입니다. 자유게시판, 정보공유게시판, 질문상담게시판, 입양임보게시판, 실종목격게시판")
  private BoardType boardType;

  @NotBlank(message = "게시글 제목은 필수입니다")
  @Size(min = 2, max = 15, message = "게시글 제목은 최소 2자~15자입니다.")
  private String title;

  @NotBlank(message = "게시글 내용은 필수입니다")
  @Size(max = 300, message = "게시글 내용은 300자를 초과할 수 없습니다.")
  private String content;
  private String video_URL;
  private List<String> removeImageUrls;
}
