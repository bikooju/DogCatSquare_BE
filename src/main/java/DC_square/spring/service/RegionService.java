package DC_square.spring.service;

import DC_square.spring.domain.entity.region.City;
import DC_square.spring.domain.entity.region.District;
import DC_square.spring.domain.entity.region.Province;
import DC_square.spring.repository.region.CityRepository;
import DC_square.spring.repository.region.DistrictRepository;
import DC_square.spring.repository.region.ProvinceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RegionService {

  private final ProvinceRepository provinceRepository;
  private final CityRepository cityRepository;
  private final DistrictRepository districtRepository;

  public District findOrCreateDistrict(String provinceName, String cityName, String districtName) {
    // 저장되지 않은 지역정보 입력시 오류 !
    // 1. Province 찾기
    Province province = provinceRepository.findByName(provinceName)
        .orElseThrow(() -> new RuntimeException(
            String.format("존재하지 않는 시/도입니다: %s", provinceName)));

    // 2. City 찾기
    City city = cityRepository.findByNameAndProvince(cityName, province)
        .orElseThrow(() -> new RuntimeException(
            String.format("존재하지 않는 시/구입니다: %s", cityName)));

    // 3. District 찾기
    return districtRepository.findByNameAndCity(districtName, city)
        .orElseThrow(() -> new RuntimeException(
            String.format("존재하지 않는 동입니다: %s", districtName)));
  }
}