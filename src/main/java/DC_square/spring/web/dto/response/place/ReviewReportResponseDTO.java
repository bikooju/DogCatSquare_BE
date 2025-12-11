package DC_square.spring.web.dto.response.place;

import DC_square.spring.domain.enums.ReportType;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReviewReportResponseDTO {

  private Long id;
  private Long reporterId;
  private Long reportedUserId;
  private String reportedUserNickname;
  private Long reviewId;
  private String reviewContent;
  private ReportType reportType;
  private String otherReason;
  private LocalDateTime createdAt;
}
