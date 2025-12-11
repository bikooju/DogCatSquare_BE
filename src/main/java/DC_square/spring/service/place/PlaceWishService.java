package DC_square.spring.service.place;

import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.place.Place;
import DC_square.spring.domain.entity.place.PlaceWish;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.repository.place.PlaceRepository;
import DC_square.spring.repository.place.PlaceWishRepository;
import DC_square.spring.web.dto.response.place.PlaceResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PlaceWishService {

  private final PlaceWishRepository placeWishRepository;
  private final UserRepository userRepository;
  private final PlaceRepository placeRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final PlaceService placeService;

  // 마이 장소 위시리스트 토글
  public boolean togglePlaceWish(String token, Long placeId) {
    String userEmail = jwtTokenProvider.getUserEmail(token);
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> new IllegalArgumentException("해당 장소가 존재하지 않습니다."));

    Optional<PlaceWish> existingWish = placeWishRepository.findByUserIdAndPlaceId(user.getId(),
        placeId);

    if (existingWish.isPresent()) {
      placeWishRepository.delete(existingWish.get());
      return false;  // 위시리스트에서 삭제되었으므로 false 반환
    } else {
      PlaceWish placeWish = PlaceWish.builder()
          .user(user)
          .place(place)
          .build();
      placeWishRepository.save(placeWish);
      return true;  // 위시리스트에 추가되었으므로 true 반환
    }
  }

}
