package DC_square.spring.web.dto.request.community;

import DC_square.spring.domain.enums.BoardType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class BoardRequestDto {

  @NotBlank(message = "게시판 이름은 고정입니다.  자유게시판,\n"
      + "    정보공유게시판,\n"
      + "    질문상담게시판,\n"
      + "    입양임보게시판,\n"
      + "    실종목격게시판")
  private BoardType boardType;

  @NotBlank(message = "게시글 설명은 필수입니다")
  @Size(max = 20, message = "게시판 설명은 20자를 초과할 수 없습니다")
  private String content;

  @Size(max = 3, message = "키워드는 최대 3개까지만 입력 가능합니다.")
  private List<String> keywords;

}
