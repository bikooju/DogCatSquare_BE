package DC_square.spring.constant;

public class WeatherConstants {

  //url
  public static final String WEATHER_API_BASE_URL = "https://apihub.kma.go.kr/api/typ02/openApi/VilageFcstInfoService_2.0";
  public static final String WEATHER_FORECAST_URL = WEATHER_API_BASE_URL + "/getVilageFcst";

  // 날씨 카테고리
  public static final String TEMPERATURE = "TMP";  // 1시간 기온
  public static final String MAX_TEMP = "TMX"; // 최고 기온
  public static final String MIN_TEMP = "TMN";  // 최저 기온
  public static final String RAIN_TYPE = "PTY"; // 강수 형태
  public static final String SKY = "SKY";  // 하늘 상태
  public static final String WIND_SPEED = "WSD";  // 풍속
  public static final String RAIN_PROBABILITY = "POP"; //강수 확률

  // S3 이미지 url
  public static final String DOG_SUN_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/sun_dog.png";
  public static final String DOG_RAIN_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/rain_dog.png";
  public static final String DOG_SNOW_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/snow_dog.png";
  public static final String DOG_WIND_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/wind_dog.png";
  public static final String DOG_BOX_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/box_dog.png";
  public static final String DOG_HOSPITAL_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/hospital_dog.png";

  public static final String CAT_SUN_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/sun_cat.png";
  public static final String CAT_RAIN_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/rain_cat.png";
  public static final String CAT_SNOW_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/snow_cat.png";
  public static final String CAT_WIND_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/wind_cat.png";
  public static final String CAT_BOX_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/box_cat.png";
  public static final String CAT_HOSPITAL_IMAGE = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/weather/hospital_cat.png";

  // 날씨 파라미터 임계값
  public static final double STRONG_WIND_SPEED = 7.0;  // 강풍 기준 (m/s)
}