package DC_square.spring.web.dto.request.place;

import DC_square.spring.domain.enums.ReportType;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ReviewReportRequestDTO {

  private ReportType reportType;

  @Size(min = 10, max = 50, message = "기타 사유는 10자 이상 50자 이하로 입력해주세요.")
  private String otherReason;
}
