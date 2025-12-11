package DC_square.spring.web.controller.place;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.service.place.PlaceWishService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "PlaceWish", description = "장소 위시리스트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/wishlist/places/{placeId}")
public class PlaceWishController {

  private final PlaceWishService placeWishService;
  private final JwtTokenProvider jwtTokenProvider;

  // 장소 위시리스트 토글 API
  @Operation(summary = "장소 위시리스트 추가 API")
  @PostMapping
  public ApiResponse<Boolean> toggleWish(
      @PathVariable("placeId") Long placeId,
      HttpServletRequest request
  ) {
    String token = jwtTokenProvider.resolveToken(request);
    Boolean isWishId = placeWishService.togglePlaceWish(token, placeId);
    return ApiResponse.onSuccess(isWishId);
  }
}
