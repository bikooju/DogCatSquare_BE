package DC_square.spring.service.WalkService;

import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.domain.entity.Pet;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.walk.Walk;
import DC_square.spring.domain.entity.walk.WalkWish;
import DC_square.spring.domain.enums.Special;
import DC_square.spring.repository.PetRepository;
import DC_square.spring.repository.WalkRepository.WalkRepository;
import DC_square.spring.repository.WalkRepository.WalkWishRepository;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.web.dto.response.walk.WalkResponseDto;
import DC_square.spring.web.dto.response.walk.WalkWishResponseDto;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalkWishService {

  private final WalkWishRepository walkWishRepository;
  private final WalkRepository walkRepository;
  private final UserRepository userRepository;
  private final PetRepository petRepository;
  private final JwtTokenProvider jwtTokenProvider;

  public WalkWishResponseDto addWalkWish(String token, Long walkId) {
    String userEmail = jwtTokenProvider.getUserEmail(token);
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    Walk walk = walkRepository.findById(walkId)
        .orElseThrow(() -> new RuntimeException("산책로를 찾을 수 없습니다."));

    if (walkWishRepository.existsByUserAndWalk(user, walk)) {
      throw new RuntimeException("이미 위시리스트에 추가된 산책로입니다.");
    }

    WalkWish walkWish = new WalkWish(user, walk, true);
    walkWishRepository.save(walkWish);

    return WalkWishResponseDto.builder()
        .status(200)
        .success(true)
        .message("위시리스트에 추가했습니다.")
        .build();
  }

  public WalkWishResponseDto cancelWalkWish(String token, Long walkId) {
    String userEmail = jwtTokenProvider.getUserEmail(token);
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    Walk walk = walkRepository.findById(walkId)
        .orElseThrow(() -> new RuntimeException("산책로를 찾을 수 없습니다."));

    WalkWish walkWish = walkWishRepository.findByUserAndWalk(user, walk)
        .orElseThrow(() -> new RuntimeException("위시리스트에 해당 산책로가 없습니다."));

    walkWishRepository.delete(walkWish);

    return WalkWishResponseDto.builder()
        .status(200)
        .success(true)
        .message("위시리스트에서 제거했습니다.")
        .build();
  }

  public WalkResponseDto viewWishlist(String token) {
    String userEmail = jwtTokenProvider.getUserEmail(token);
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    List<WalkWish> walkWishes = walkWishRepository.findByUserAndIsWished(user, true);

    List<WalkResponseDto.WalkDto> walkDtos = walkWishes.stream()
        .map(walkWish -> {
          Walk walk = walkWish.getWalk();
          User createdByUser = walk.getCreatedBy();
          Pet pet = petRepository.findByUser(createdByUser);
          String breed = (pet != null) ? pet.getBreed() : null;

          return WalkResponseDto.WalkDto.builder()
              .walkId(walk.getId())
              .title(walk.getTitle())
              .description(walk.getDescription())
              .walkImageUrl(walk.getWalkImageUrl())
              .reviewCount(walk.getReviewCount())
              .distance(walk.getDistance())
              .time(walk.getTime())
              .difficulty(walk.getDifficulty().name())
              .special(walk.getSpecials().stream()
                  .map(special -> WalkResponseDto.SpecialDto.builder()
                      .type(special.getSpecialType().name())
                      .customValue(
                          special.getSpecialType() == Special.OTHER ? special.getCustomValue()
                              : null)
                      .build())
                  .collect(Collectors.toList()))
              .coordinates(walk.getCoordinates().stream()
                  .map(coord -> WalkResponseDto.CoordinateDto.builder()
                      .latitude(coord.getLatitude())
                      .longitude(coord.getLongitude())
                      .sequence(coord.getSequence())
                      .build())
                  .collect(Collectors.toList()))
              .createdAt(walk.getCreatedAt())
              .updatedAt(walk.getUpdatedAt())
              .createdBy(WalkResponseDto.CreatedByDto.builder()
                  .nickname(createdByUser.getNickname())
                  .profileImageUrl(createdByUser.getProfileImageUrl())
                  .breed(breed)
                  .build())
              .build();
        })
        .collect(Collectors.toList());

    return WalkResponseDto.builder()
        .walks(walkDtos)
        .build();
  }

}