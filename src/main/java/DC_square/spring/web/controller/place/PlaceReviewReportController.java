package DC_square.spring.web.controller.place;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.service.place.PlaceReviewReportService;
import DC_square.spring.web.dto.request.place.ReviewReportRequestDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PlaceReviewReport", description = "장소 리뷰 신고 관련 API입니다.")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/places/place-reviews")
public class PlaceReviewReportController {

  private final PlaceReviewReportService placeReviewReportService;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(summary = "리뷰 신고 API", description = "리뷰를 신고합니다.")
  @PostMapping("/{place-reviewId}/report")
  public ApiResponse<Long> reportReview(
      @PathVariable("place-reviewId") Long placeReviewId,
      @Valid @RequestBody ReviewReportRequestDTO request,
      HttpServletRequest httpRequest
  ) {
    String Token = jwtTokenProvider.resolveToken(httpRequest);
    Long reportId = placeReviewReportService.reportReview(Token, placeReviewId, request);
    return ApiResponse.onSuccess(reportId);
  }
}
