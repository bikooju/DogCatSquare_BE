package DC_square.spring.web.controller.dday;


import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.domain.entity.User;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.service.dday.DdayService;
import DC_square.spring.web.dto.request.dday.DdayRequestDto;
import DC_square.spring.web.dto.request.dday.DdayUpdateRequestDto;
import DC_square.spring.web.dto.response.dday.DdayResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "D-day", description = "D-day 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/ddays")
public class DdayController {

  private final DdayService ddayService;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;


  @Operation(summary = "D-day 생성 API", description = "새로운 D-day를 생성하는 API입니다.", security = @SecurityRequirement(name = "Authorization"))
  @PostMapping
  public ApiResponse<DdayResponseDto> createDday(
      HttpServletRequest request,
      @Valid @RequestBody DdayRequestDto ddayRequest) {
    String token = jwtTokenProvider.resolveToken(request); //해더에서 JWT 토큰 추출
    String userEmail = jwtTokenProvider.getUserEmail(token); //JWT 토큰을 복호화해서 저장했던 이메일을 가져온다

    //추출한 이메일로 db에서 사용자 정보를 조회
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    // 디데이 생성
    DdayResponseDto response = ddayService.createDday(user.getId(), ddayRequest);
    return ApiResponse.onSuccess(response);
  }

  @Operation(summary = "사용자별 D-day 조회 API", description = "사용자의 모든 D-day를 조회하는 API입니다.", security = @SecurityRequirement(name = "Authorization"))
  @GetMapping
  public ApiResponse<List<DdayResponseDto>> getDdays(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    String userEmail = jwtTokenProvider.getUserEmail(token);

    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    List<DdayResponseDto> response = ddayService.getDdaysByUser(user.getId());
    return ApiResponse.onSuccess(response);
  }

  @Operation(summary = "D-day 수정 API", description = "D-day를 수정하는 API입니다.", security = @SecurityRequirement(name = "Authorization"))
  @PutMapping("/{ddayId}")
  public ApiResponse<DdayResponseDto> updateDday(
      HttpServletRequest request,
      @PathVariable Long ddayId,
      @Valid @RequestBody DdayUpdateRequestDto updateRequest) {
    String token = jwtTokenProvider.resolveToken(request);
    String userEmail = jwtTokenProvider.getUserEmail(token);

    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    DdayResponseDto response = ddayService.updateDday(user.getId(), ddayId, updateRequest);
    return ApiResponse.onSuccess(response);
  }


  @Operation(summary = "D-day 삭제 API", description = "D-day를 삭제하는 API입니다.", security = @SecurityRequirement(name = "Authorization"))
  @DeleteMapping("/{ddayId}")
  public ApiResponse<Void> deleteDday(
      HttpServletRequest request,
      @PathVariable Long ddayId) {
    String token = jwtTokenProvider.resolveToken(request);
    String userEmail = jwtTokenProvider.getUserEmail(token);

    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    ddayService.deleteDday(user.getId(), ddayId);
    return ApiResponse.onSuccess(null);
  }
}