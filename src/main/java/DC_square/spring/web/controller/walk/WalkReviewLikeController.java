package DC_square.spring.web.controller.walk;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.service.WalkService.WalkReviewLikeService;
import DC_square.spring.web.dto.request.walk.WalkReviewLikeRequestDto;
import DC_square.spring.web.dto.response.walk.WalkReviewLikeResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/walks/{walkId}/reviews/{reviewId}/likes")
public class WalkReviewLikeController {

  private final WalkReviewLikeService walkReviewLikeService;

  @Operation(summary = "산책로 후기 좋아요 추가 API", description = "특정 산책로 후기에서 좋아요를 추가하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "좋아요 추가 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON500", description = "서버 오류")
  })
  @PostMapping
  public ApiResponse<Void> addLike(
      @PathVariable Long walkId,
      @PathVariable Long reviewId,
      @RequestBody WalkReviewLikeRequestDto requestDto
  ) {
    walkReviewLikeService.likeWalkReview(reviewId, requestDto);
    return ApiResponse.onSuccess(null, "좋아요를 추가했습니다.");
  }

  @Operation(summary = "산책로 후기 좋아요 취소 API", description = "특정 산책로 후기에서 좋아요를 취소하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "좋아요 취소 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "잘못된 요청"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON500", description = "서버 오류")
  })
  @DeleteMapping("/{likeId}")
  public ApiResponse<Void> cancelLike(
      @PathVariable Long walkId,
      @PathVariable Long reviewId,
      @PathVariable Long likeId
  ) {
    WalkReviewLikeResponseDto responseDto = walkReviewLikeService.cancelLike(likeId);
    return ApiResponse.onSuccess(null, responseDto.getMessage());
  }
}
