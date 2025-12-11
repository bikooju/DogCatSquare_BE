package DC_square.spring.domain.enums;

import DC_square.spring.constant.WeatherConstants;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum WeatherStatus {
  SUNNY("맑음"),
  CLOUDY("흐림"),
  RAINY("비"),
  SNOWY("눈"),
  WINDY("강풍");

  private final String description;

  public static WeatherStatus fromWeatherData(String pty, String sky, double windSpeed) {
    // 강수 형태 체크
    if (pty != null) {
      switch (pty) {
        case "1":
        case "2":
          return RAINY;  // 비 또는 비/눈
        case "3":
          return SNOWY;  // 눈
      }
    }

    // 강풍 체크
    if (windSpeed >= WeatherConstants.STRONG_WIND_SPEED) {
      return WINDY;
    }

    // 하늘 상태 체크
    if ("1".equals(sky)) {
      return SUNNY;
    }

    return CLOUDY;
  }
}