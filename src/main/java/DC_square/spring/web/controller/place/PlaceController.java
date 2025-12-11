package DC_square.spring.web.controller.place;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.service.place.PlaceService;
import DC_square.spring.web.dto.request.place.FilteredPlaceRequestDTO;
import DC_square.spring.web.dto.request.place.LocationRequestDTO;
import DC_square.spring.web.dto.request.place.PlaceCreateRequestDTO;
import DC_square.spring.web.dto.request.place.PlaceUserInfoUpdateDTO;
import DC_square.spring.web.dto.response.place.PlaceDetailResponseDTO;
import DC_square.spring.web.dto.response.place.PlacePageResponseDTO;
import DC_square.spring.web.dto.response.place.PlaceResponseDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Place", description = "장소 관련 API")
@RestController
@RequestMapping("/api/places")
@RequiredArgsConstructor
public class PlaceController {

  private final PlaceService placeService;
  private final JwtTokenProvider jwtTokenProvider;

  // 장소 전체 조회 API
  @Operation(summary = "키워드로 장소 검색 API")
  @PostMapping("/search")
  public ApiResponse<PlacePageResponseDTO<PlaceResponseDTO>> searchPlaces(
      @RequestParam String keyword,
      @RequestBody LocationRequestDTO location,
      @RequestParam(defaultValue = "0") int page
  ) {
    return ApiResponse.onSuccess(placeService.searchPlacesByKeyword(keyword, location, page, 10));
  }

  @Operation(summary = "카테고리 기반 주변 장소 검색 API")
  @PostMapping("/nearby")
  public ApiResponse<PlacePageResponseDTO<PlaceResponseDTO>> searchNearbyPlaces(
      @RequestBody LocationRequestDTO location,
      @RequestParam(defaultValue = "0") int page
  ) {
    return ApiResponse.onSuccess(placeService.findNearbyPlaces(location, page, 20));
  }

  // 장소 생성 API (관리자용)
  @Operation(summary = "장소 생성 API")
  @PostMapping
  public ApiResponse<Long> createPlace(
      @RequestBody PlaceCreateRequestDTO request
  ) {
    Long placeId = placeService.createPlace(request);
    return ApiResponse.onSuccess(placeId);
  }

  // 장소 정보 수정
  @Operation(summary = "장소 정보 수정 API(데모데이용)")
  @PostMapping("/{placeId}/update")
  public ApiResponse<String> updatePlace(
      @PathVariable("placeId") Long placeId,
      @RequestBody PlaceCreateRequestDTO request
  ) {
    placeService.updatePlace(placeId, request);
    return ApiResponse.onSuccess("장소 정보가 성공적으로 수정되었습니다.");
  }

  // 장소 상세 조회 API
  @Operation(summary = "장소 상세 조회 API")
  @PostMapping("/{placeId}")
  public ApiResponse<PlaceDetailResponseDTO> getPlaceById(
      @PathVariable("placeId") Long placeId,
      @RequestBody LocationRequestDTO location,
      HttpServletRequest request
  ) {
    String token = jwtTokenProvider.resolveToken(request);
    placeService.increaseViewCount(placeId);
    PlaceDetailResponseDTO place = placeService.findPlaceDetailById(placeId, token, location);
    return ApiResponse.onSuccess(place);
  }

  // 마이 위시 장소 조회 API
  @Operation(summary = "마이 위시 장소 조회 API")
  @PostMapping("/wishlist")
  public ApiResponse<List<PlaceResponseDTO>> getWishList(
      HttpServletRequest request,
      @RequestBody LocationRequestDTO location
  ) {
    String token = jwtTokenProvider.resolveToken(request);
    List<PlaceResponseDTO> places = placeService.findWishList(token, location);
    return ApiResponse.onSuccess(places);
  }

  // 지역별 핫 플레이스 조회 API
  @Operation(summary = "지역별 핫 플레이스 조회 API", description = "위시가 많은 순서로 정렬")
  @PostMapping("/hot/{cityId}")
  public ApiResponse<List<PlaceResponseDTO>> getHotPlacesByCity(
      @PathVariable("cityId") Long cityId,
      @RequestBody LocationRequestDTO location
  ) {
    List<PlaceResponseDTO> places = placeService.findHotPlacesByCity(cityId, location);
    return ApiResponse.onSuccess(places);
  }

  // 장소 데이터 입력 받기 API
  @Operation(summary = "장소 데이터 입력 받기 API")
  @PostMapping("/{placeId}/data")
  public ApiResponse<String> updatePlaceUserInfo(
      @PathVariable("placeId") Long placeId,
      @RequestBody PlaceUserInfoUpdateDTO updateDTO
  ) {
    placeService.updatePlaceUserInfo(placeId, updateDTO);
    return ApiResponse.onSuccess("장소 정보가 성공적으로 업데이트 되었습니다.");
  }

  // 장소 전체 필터링 조회
  @Operation(summary = "필터링된 장소 조회 API")
  @PostMapping("/filter")
  public ApiResponse<PlacePageResponseDTO<PlaceResponseDTO>> getFilteredPlaces(
      @RequestBody FilteredPlaceRequestDTO filterRequest,
      @RequestParam(defaultValue = "0") int page
  ) {
    return ApiResponse.onSuccess(placeService.findPlacesWithFilters(
            filterRequest.getLocation(),
            filterRequest.getIs24Hours(),
            filterRequest.getHasParking(),
            filterRequest.getIsCurrentlyOpen(),
            page,
            10
        )
    );
  }
}