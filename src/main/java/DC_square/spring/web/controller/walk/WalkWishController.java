package DC_square.spring.web.controller.walk;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.service.WalkService.WalkWishService;
import DC_square.spring.web.dto.response.walk.WalkResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist/walks")
public class WalkWishController {

  private final WalkWishService walkWishService;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(summary = "위시리스트 등록 API", description = "산책로를 위시리스트에 추가하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 위시리스트 등록 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON401", description = "로그인 필요"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON404", description = "산책로를 찾을 수 없음")
  })
  @PostMapping("/{walkId}")
  public ApiResponse<String> addWishlist(
      @PathVariable Long walkId,
      HttpServletRequest request
  ) {
    String token = jwtTokenProvider.resolveToken(request);
    walkWishService.addWalkWish(token, walkId);
    return ApiResponse.onSuccess("위시리스트에 추가되었습니다.");
  }

  @Operation(summary = "위시리스트 등록 취소 API", description = "산책로를 위시리스트에서 취소하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 위시리스트 등록 취소 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON401", description = "로그인 필요"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON404", description = "산책로를 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "이미 취소된 상태")
  })
  @DeleteMapping("/{walkId}/cancel")
  public ApiResponse<String> cancelWishlist(
      @PathVariable Long walkId,
      HttpServletRequest request
  ) {
    String token = jwtTokenProvider.resolveToken(request);
    walkWishService.cancelWalkWish(token, walkId);
    return ApiResponse.onSuccess("위시리스트에서 취소되었습니다.");
  }

  @Operation(summary = "산책로 위시리스트 조회 API", description = "사용자가 위시리스트에 추가한 산책로 목록을 조회하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 위시리스트 조회 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON401", description = "로그인 필요")
  })
  @GetMapping
  public ApiResponse<WalkResponseDto> viewWishlist(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    WalkResponseDto walkResponseDto = walkWishService.viewWishlist(token);
    return ApiResponse.onSuccess(walkResponseDto);
  }
}