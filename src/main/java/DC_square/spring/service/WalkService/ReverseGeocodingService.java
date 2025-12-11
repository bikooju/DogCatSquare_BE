package DC_square.spring.service.WalkService;

import DC_square.spring.config.GoogleMapsConfig;
import DC_square.spring.web.dto.response.walk.GeocodingResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
@RequiredArgsConstructor
public class ReverseGeocodingService {

  private static final Logger logger = LoggerFactory.getLogger(ReverseGeocodingService.class);

  private final GoogleMapsConfig googleMapsConfig;
  private final RestTemplate restTemplate;

  public String getAddressFromCoordinates(double latitude, double longitude) {
    String apiKey = googleMapsConfig.getApiKey();
    logger.info("사용하는 API 키: {}", apiKey);

    String url = "https://maps.googleapis.com/maps/api/geocode/json";

    String requestUrl = UriComponentsBuilder.fromHttpUrl(url)
        .queryParam("latlng", latitude + "," + longitude)
        .queryParam("language", "ko")
        .queryParam("key", apiKey)
        .toUriString();

    logger.info("Reverse Geocoding 요청 URL: {}", requestUrl);

    try {
      GeocodingResponse response = restTemplate.getForObject(requestUrl, GeocodingResponse.class);
      logger.info("Google Maps API 응답: {}", response);

      if (response != null) {
        logger.info("응답 status: {}", response.getStatus());
      }

      if (response != null && "OK".equals(response.getStatus()) &&
          response.getResults() != null && !response.getResults().isEmpty()) {
        String address = response.getResults().get(0).getFormattedAddress();
        logger.info("Reverse Geocoding 응답 주소: {}", address);
        return address;
      } else {
        logger.warn("Reverse Geocoding 결과 없음. 응답: {}", response);
        return "주소를 찾을 수 없습니다. (API 응답 status: " + (response != null ? response.getStatus()
            : "null") + ")";
      }
    } catch (Exception e) {
      logger.error("Reverse Geocoding 요청 실패: {}", e.getMessage());
      return "주소를 가져오는 중 오류가 발생했습니다.";
    }
  }
}
