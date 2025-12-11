package DC_square.spring.web.controller;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.domain.entity.User;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.service.WeatherService;
import DC_square.spring.web.dto.response.WeatherResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Weather", description = "날씨 관련 API")
@RestController
@RequestMapping("/api/weather")
@RequiredArgsConstructor
@Slf4j
public class WeatherController {

  private final WeatherService weatherService;
  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  @Operation(
      summary = "현재 날씨 조회 API",
      description = "사용자의 위치 기반 날씨 정보와 반려동물 맞춤 메시지를 조회합니다.",
      security = @SecurityRequirement(name = "Authorization")
  )
  @GetMapping("/current")
  public ApiResponse<WeatherResponseDto> getCurrentWeather(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    if (token == null || !jwtTokenProvider.validateToken(token)) {
      throw new RuntimeException("유효하지 않은 토큰입니다.");
    }

    String userEmail = jwtTokenProvider.getUserEmail(token);
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    WeatherResponseDto weatherResponse = weatherService.getCurrentWeather(user.getId());

    log.info("Weather 조회 API 응답값: {}", weatherResponse);

    return ApiResponse.onSuccess(weatherResponse);
  }
}