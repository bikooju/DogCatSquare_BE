package DC_square.spring.service.place;

import DC_square.spring.config.GoogleMapsConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
@RequiredArgsConstructor
public class GooglePlacesService {

  private final GoogleMapsConfig googleMapsConfig;
  private final RestTemplate restTemplate;
  private static final String PLACES_API_BASE_URL = "https://maps.googleapis.com/maps/api/place";
  private static final String TRANSLATE_API_BASE_URL = "https://translation.googleapis.com/language/translate/v2";

  @lombok.Value
  private static class NameValuePair {

    String type;
    String keyword;
  }

  // 근처 장소 검색
  public Map<String, Object> searchNearbyPlaces(double latitude, double longitude) {
    // 타입과 키워드를 매핑
    Map<String, NameValuePair> searchMap = new HashMap<>();
    searchMap.put("veterinary_care", new NameValuePair("veterinary_care", null));  // 동물병원은 타입으로
    searchMap.put("park", new NameValuePair("park", null));                        // 공원은 타입으로
    searchMap.put("pet_store", new NameValuePair("pet_store", null));             // 펫스토어는 타입으로
    searchMap.put("cafe", new NameValuePair(null, "애견카페"));                    // 애견카페는 키워드로만
    searchMap.put("hotel", new NameValuePair(null, "애견호텔"));

    List<Map<String, Object>> allResults = new ArrayList<>();

    for (NameValuePair pair : searchMap.values()) {
      UriComponentsBuilder builder = UriComponentsBuilder
          .fromHttpUrl(PLACES_API_BASE_URL + "/nearbysearch/json")
          .queryParam("location", latitude + "," + longitude)
          .queryParam("radius", 3000)
          .queryParam("language", "ko")
          .queryParam("key", googleMapsConfig.getApiKey());

      // type이 있는 경우에만 추가
      if (pair.type != null) {
        builder.queryParam("type", pair.type);
      }

      // keyword가 있는 경우에만 추가
      if (pair.keyword != null) {
        builder.queryParam("keyword", pair.keyword);
      }

      String url = builder.build().toUriString();
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null && response.get("results") != null) {
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        allResults.addAll(results);
      }
    }

    // 중복 제거
    List<Map<String, Object>> uniqueResults = new ArrayList<>();
    Set<String> seenPlaceIds = new HashSet<>();

    for (Map<String, Object> result : allResults) {
      String placeId = (String) result.get("place_id");
      if (seenPlaceIds.add(placeId)) {
        uniqueResults.add(result);
      }
    }

    Map<String, Object> finalResponse = new HashMap<>();
    finalResponse.put("results", uniqueResults);

    return finalResponse;
  }

  // 키워드로 검색
  public Map<String, Object> searchPlacesByKeyword(String keyword) {
    // 원본 키워드를 보존하기 위한 변수
    String originalKeyword = keyword;
    List<Map<String, Object>> allResults = new ArrayList<>();

    // 검색 조건 설정
    Map<String, NameValuePair> searchMap = new HashMap<>();
    searchMap.put("veterinary_care",
        new NameValuePair("veterinary_care", keyword));  // 동물병원 타입 + 키워드
    searchMap.put("park", new NameValuePair("park", keyword));                        // 공원 타입 + 키워드
    searchMap.put("pet_store",
        new NameValuePair("pet_store", keyword));             // 펫스토어 타입 + 키워드
    searchMap.put("cafe", new NameValuePair(null, "애견카페 " + keyword));           // 애견카페 키워드
    searchMap.put("hotel", new NameValuePair(null, "애견호텔 " + keyword));          // 애견호텔 키워드

    for (NameValuePair pair : searchMap.values()) {
      UriComponentsBuilder builder = UriComponentsBuilder
          .fromHttpUrl(PLACES_API_BASE_URL + "/textsearch/json")
          .queryParam("language", "ko")
          .queryParam("key", googleMapsConfig.getApiKey());

      // type이 있는 경우
      if (pair.type != null) {
        builder.queryParam("type", pair.type);
        builder.queryParam("query", pair.keyword);
      }
      // type이 없는 경우 (애견카페, 애견호텔)
      else if (pair.keyword != null) {
        builder.queryParam("query", pair.keyword);
      }

      String url = builder.build().toUriString();
      Map<String, Object> response = restTemplate.getForObject(url, Map.class);
      if (response != null && response.get("results") != null) {
        List<Map<String, Object>> results = (List<Map<String, Object>>) response.get("results");
        // 결과를 필터링하여 추가
        results.stream()
            .filter(this::isPetRelatedPlace)
            .forEach(allResults::add);
      }
    }

    // 중복 제거
    List<Map<String, Object>> uniqueResults = new ArrayList<>();
    Set<String> seenPlaceIds = new HashSet<>();

    for (Map<String, Object> result : allResults) {
      String placeId = (String) result.get("place_id");
      if (seenPlaceIds.add(placeId)) {
        uniqueResults.add(result);
      }
    }

    Map<String, Object> finalResponse = new HashMap<>();
    finalResponse.put("results", uniqueResults);

    return finalResponse;
  }

  // 장소 상세 정보 조회
  public Map<String, Object> getPlaceDetails(String placeId) {
    String url = UriComponentsBuilder
        .fromHttpUrl(PLACES_API_BASE_URL + "/details/json")
        .queryParam("place_id", placeId)
        .queryParam("fields",
            "name,formatted_phone_number,formatted_address,address_components,opening_hours,website,photos,editorial_summary,geometry")
        .queryParam("key", googleMapsConfig.getApiKey())
        .queryParam("language", "ko")
        .build()
        .toUriString();

    return restTemplate.getForObject(url, Map.class);
  }

  public String getPhotoUrl(String photoReference, Integer maxWidth) {
    return UriComponentsBuilder
        .fromHttpUrl(PLACES_API_BASE_URL + "/photo")
        .queryParam("maxwidth", maxWidth)
        .queryParam("photo_reference", photoReference)
        .queryParam("key", googleMapsConfig.getApiKey())
        .build()
        .toUriString();
  }

  private boolean isPetRelatedPlace(Map<String, Object> placeData) {
    String name = ((String) placeData.get("name")).toLowerCase();

    List<String> petKeywords = Arrays.asList(
        "애견", "펫", "동물", "쥬", "pet", "animal", "zoo",
        "강아지", "고양이", "독", "도그", "dog", "cat", "멍", "댕", "냥",
        "공원", "park", "veterinary"
    );

    return petKeywords.stream()
        .anyMatch(keyword -> name.contains(keyword));
  }
}