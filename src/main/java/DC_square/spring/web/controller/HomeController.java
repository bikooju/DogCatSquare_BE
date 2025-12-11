package DC_square.spring.web.controller;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.constant.WeatherConstants;
import DC_square.spring.domain.entity.Dday;
import DC_square.spring.domain.entity.Pet;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.enums.DogCat;
import DC_square.spring.repository.PetRepository;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.service.dday.DdayService;
import DC_square.spring.web.dto.response.WeatherResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/home")
@Tag(name = "Home", description = "홈 화면 관련 API")
public class HomeController {

  private final DdayService ddayService;
  private final UserRepository userRepository;
  private final PetRepository petRepository;
  private final JwtTokenProvider jwtTokenProvider;

  @Operation(
      summary = "홈 화면 D-day 조회 API",
      description = "사용자의 가장 가까운 D-day 정보를 조회합니다.",
      security = @SecurityRequirement(name = "Authorization")
  )
  @GetMapping("/dday")
  public ApiResponse<WeatherResponseDto> getHomeDday(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    if (token == null || !jwtTokenProvider.validateToken(token)) {
      throw new RuntimeException("유효하지 않은 토큰입니다.");
    }

    String userEmail = jwtTokenProvider.getUserEmail(token);
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    Pet pet = petRepository.findByUser(user);
    if (pet == null) {
      throw new RuntimeException("반려동물 정보를 찾을 수 없습니다.");
    }

    List<Dday> ddays = ddayService.getDdayEntitiesByUser(user.getId());
    Dday nearestDday = findNearestDday(ddays);

    return ApiResponse.onSuccess(createHomeResponse(pet.getDogCat(),
        user.getDistrict().getName(), nearestDday));
  }

  private Dday findNearestDday(List<Dday> ddays) {
    LocalDate today = LocalDate.now();
    return ddays.stream()
        .filter(dday -> {
          String title = dday.getTitle();
          boolean isValidTitle = title.equals("사료 구매") ||
              title.equals("패드/모래 구매") ||
              title.equals("병원 방문일");
          boolean isFutureDate = !dday.getDay().isBefore(today);
          return isValidTitle && isFutureDate;
        })
        .min(Comparator.comparing(Dday::getDay))
        .orElse(null);
  }

  private WeatherResponseDto createHomeResponse(DogCat petType, String location, Dday dday) {
    if (dday == null) {
      return WeatherResponseDto.builder()
          .location(location)
          .build();
    }

    boolean isDog = petType == DogCat.DOG;
    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy년 M월 d일");

    WeatherResponseDto.WeatherResponseDtoBuilder builder = WeatherResponseDto.builder()
        .location(location)
        .currentTemp(null)
        .maxTemp(null)
        .minTemp(null)
        .rainProbability(null)
        .ddayTitle(dday.getTitle())
        .ddayMessage("D-" + dday.getDaysLeft())
        .ddayDate(dday.getDay().format(formatter));

    switch (dday.getType()) {
      case FOOD:
        builder.mainMessage("사료 주문일이")
            .subMessage("다가오고있어요")
            .imageUrl(isDog ? WeatherConstants.DOG_BOX_IMAGE : WeatherConstants.CAT_BOX_IMAGE);
        break;
      case PAD:
        builder.mainMessage(isDog ? "패드 구매일이" : "모래 구매일이")
            .subMessage("다가오고있어요")
            .imageUrl(isDog ? WeatherConstants.DOG_BOX_IMAGE : WeatherConstants.CAT_BOX_IMAGE);
        break;
      case HOSPITAL:
        builder.mainMessage("병원 방문일이")
            .subMessage("다가오고있어요")
            .imageUrl(
                isDog ? WeatherConstants.DOG_HOSPITAL_IMAGE : WeatherConstants.CAT_HOSPITAL_IMAGE);
        break;
      default:
        builder.mainMessage("디데이가")
            .subMessage("다가오고있어요")
            .imageUrl(isDog ? WeatherConstants.DOG_SUN_IMAGE : WeatherConstants.CAT_SUN_IMAGE);
    }

    return builder.build();
  }
}