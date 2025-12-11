package DC_square.spring.web.controller.walk;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.service.WalkService.WalkService;
import DC_square.spring.web.dto.request.walk.WalkCreateRequestDto;
import DC_square.spring.web.dto.request.walk.WalkRequestDto;
import DC_square.spring.web.dto.response.walk.WalkCreateResponseDto;
import DC_square.spring.web.dto.response.walk.WalkDetailResponseDto;
import DC_square.spring.web.dto.response.walk.WalkResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Service
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class WalkController {

  private final WalkService walkService;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(summary = "산책로 목록 조회 api", description = "산책로 목록을 조회하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @PostMapping("/walks")
  public ApiResponse<WalkResponseDto> viewWalkList(
      @RequestBody WalkRequestDto walkRequestDto
  ) {
    WalkResponseDto walkResponseDto = walkService.viewWalkList(walkRequestDto);
    return ApiResponse.onSuccess(walkResponseDto);
  }

  @Operation(summary = "산책로 세부 정보 조회 API", description = "특정 산책로의 세부 정보를 조회하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON404", description = "산책로를 찾을 수 없음")
  })
  @Parameter(name = "walkId", description = "조회할 산책로의 ID")
  @GetMapping("/walks/{walkId}")
  public ApiResponse<WalkDetailResponseDto> getWalkDetails(
      @PathVariable Long walkId
  ) {
    WalkDetailResponseDto walkDetailResponseDto = walkService.getWalkDetails(walkId);
    return ApiResponse.onSuccess(walkDetailResponseDto);
  }

  @Operation(summary = "산책로 등록 API", description = "새로운 산책로를 등록하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON201", description = "산책로 등록 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "입력 데이터가 유효하지 않음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON401", description = "로그인 필요"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON500", description = "서버 오류")
  })
  @PostMapping(value = "/walks/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<WalkCreateResponseDto> createWalk(
      @RequestPart(value = "walkCreateRequestDto") WalkCreateRequestDto walkCreateRequestDto,
      @RequestPart(value = "walkReviewImages") List<MultipartFile> images,
      HttpServletRequest request
  ) {
    String token = jwtTokenProvider.resolveToken(request);
    WalkCreateResponseDto walkCreateResponseDto = walkService.createWalk(walkCreateRequestDto,
        token, images);
    return ApiResponse.onSuccess(walkCreateResponseDto);
  }

  @Operation(summary = "산책로 삭제 API", description = "특정 산책로를 삭제하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 삭제 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON404", description = "산책로를 찾을 수 없음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON403", description = "삭제 권한 없음")
  })
  @Parameter(name = "walkId", description = "삭제할 산책로의 ID", required = true)
  @DeleteMapping("/walks/{walkId}")
  public ApiResponse<Void> deleteWalk(
      @PathVariable Long walkId,
      HttpServletRequest request
  ) {
    String token = jwtTokenProvider.resolveToken(request);
    walkService.deleteWalk(walkId, token);
    return ApiResponse.onSuccess(null, "산책로 삭제에 성공했습니다.");
  }

  @Operation(summary = "특정 산책로의 목록 조회 api", description = "특정 산책로의 목록을 조회하는 API입니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 검색 성공"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON400", description = "입력 데이터가 유효하지 않음"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON404", description = "검색된 결과가 없음")
  })
  @Parameter(name = "title", description = "조회할 산책로 제목", required = true)
  @GetMapping("/walks/search")
  public WalkResponseDto searchWalks(@RequestParam String title) {
    return walkService.searchWalks(title);
  }
}