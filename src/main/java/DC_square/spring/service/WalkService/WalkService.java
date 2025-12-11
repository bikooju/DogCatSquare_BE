package DC_square.spring.service.WalkService;

import DC_square.spring.config.S3.AmazonS3Manager;
import DC_square.spring.config.S3.Uuid;
import DC_square.spring.config.S3.UuidRepository;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.domain.entity.Coordinate;
import DC_square.spring.domain.entity.Pet;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.walk.Walk;
import DC_square.spring.domain.entity.walk.WalkSpecial;
import DC_square.spring.domain.enums.Special;
import DC_square.spring.repository.PetRepository;
import DC_square.spring.repository.WalkRepository.WalkRepository;
import DC_square.spring.repository.WalkRepository.WalkSpecialRepository;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.web.dto.request.walk.WalkCreateRequestDto;
import DC_square.spring.web.dto.request.walk.WalkRequestDto;
import DC_square.spring.web.dto.response.walk.WalkCreateResponseDto;
import DC_square.spring.web.dto.response.walk.WalkDetailResponseDto;
import DC_square.spring.web.dto.response.walk.WalkResponseDto;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class WalkService {

  private final WalkRepository walkRepository;
  private final UserRepository userRepository;
  private final PetRepository petRepository;
  private final WalkSpecialRepository walkSpecialRepository;
  private final ReverseGeocodingService reverseGeocodingService;
  private final JwtTokenProvider jwtTokenProvider;
  private final AmazonS3Manager s3Manager;
  private final UuidRepository uuidRepository;

  public WalkResponseDto viewWalkList(WalkRequestDto walkRequestDto) {
    // 사용자 요청에 따라 가까운 산책로를 가져오는 로직 (예: 위도와 경도를 기준으로 검색)
    List<Walk> walks = walkRepository.findNearbyWalks(
        walkRequestDto.getLatitude(),
        walkRequestDto.getLongitude(),
        walkRequestDto.getRadius()
    );

    List<WalkResponseDto.WalkDto> walkDtos = walks.stream()
        .map(walk -> {
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

  public WalkDetailResponseDto getWalkDetails(Long walkId) {
    Walk walk = walkRepository.findById(walkId)
        .orElseThrow(() -> new IllegalArgumentException("Walk not found for id: " + walkId));

    WalkResponseDto.CoordinateDto startCoordinate = walk.getCoordinates().stream()
        .filter(coord -> coord.getSequence() == 1)
        .map(coord -> WalkResponseDto.CoordinateDto.builder()
            .latitude(coord.getLatitude())
            .longitude(coord.getLongitude())
            .sequence(coord.getSequence())
            .build())
        .findFirst()
        .orElse(null);

    WalkResponseDto.CoordinateDto endCoordinate = walk.getCoordinates().stream()
        .filter(coord -> coord.getSequence() == walk.getCoordinates().size())
        .map(coord -> WalkResponseDto.CoordinateDto.builder()
            .latitude(coord.getLatitude())
            .longitude(coord.getLongitude())
            .sequence(coord.getSequence())
            .build())
        .findFirst()
        .orElse(null);

    String startAddress = (startCoordinate != null) ?
        reverseGeocodingService.getAddressFromCoordinates(startCoordinate.getLatitude(),
            startCoordinate.getLongitude()) : "주소 없음";

    String endAddress = (endCoordinate != null) ?
        reverseGeocodingService.getAddressFromCoordinates(endCoordinate.getLatitude(),
            endCoordinate.getLongitude()) : "주소 없음";

    User createdByUser = walk.getCreatedBy();
    Pet pet = petRepository.findByUser(createdByUser);
    String breed = (pet != null) ? pet.getBreed() : null;

    return WalkDetailResponseDto.builder()
        .walkId(walk.getId())
        .title(walk.getTitle())
        .description(walk.getDescription())
        .walkImageUrl(walk.getWalkImageUrl())
        .distance(walk.getDistance())
        .time(walk.getTime())
        .difficulty(walk.getDifficulty().name())
        .special(walk.getSpecials().stream()
            .map(special -> WalkResponseDto.SpecialDto.builder()
                .type(special.getSpecialType().name())
                .customValue(
                    special.getSpecialType() == Special.OTHER ? special.getCustomValue() : null)
                .build())
            .collect(Collectors.toList()))
        .startAddress(startAddress)
        .endAddress(endAddress)
        .createdAt(walk.getCreatedAt())
        .updatedAt(walk.getUpdatedAt())
        .createdBy(WalkResponseDto.CreatedByDto.builder()
            .nickname(createdByUser.getNickname())
            .profileImageUrl(createdByUser.getProfileImageUrl())
            .breed(breed)
            .build())
        .build();
  }


  public WalkCreateResponseDto createWalk(WalkCreateRequestDto walkCreateRequestDto, String token,
      List<MultipartFile> images) {
    if (images.isEmpty()) {
      throw new RuntimeException("후기 이미지는 필수 입니다.");
    }

    List<String> imageUrls = images.stream()
        .map(image -> {
          String uuid = UUID.randomUUID().toString();
          Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
          return s3Manager.uploadFile(s3Manager.generateWalk(savedUuid), image);
        })
        .collect(Collectors.toList());

    if (token == null || !jwtTokenProvider.validateToken(token)) {
      throw new IllegalArgumentException("잘못된 토큰입니다.");
    }

    String userEmail = jwtTokenProvider.getUserEmail(token);

    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Walk walk = Walk.builder()
        .title(walkCreateRequestDto.getTitle())
        .description(walkCreateRequestDto.getDescription())
        .walkImageUrl(imageUrls)
        .distance(walkCreateRequestDto.getDistance())
        .time(walkCreateRequestDto.getTime())
        .difficulty(walkCreateRequestDto.getDifficulty())
        .coordinates(walkCreateRequestDto.getCoordinates().stream()
            .map(coordDto -> Coordinate.builder()
                .latitude(coordDto.getLatitude())
                .longitude(coordDto.getLongitude())
                .sequence(coordDto.getSequence())
                .build())
            .collect(Collectors.toList()))
        .reviewCount(0)
        .createdBy(user)
        .build();

    Walk savedWalk = walkRepository.save(walk);

    List<WalkSpecial> specials = walkCreateRequestDto.getSpecial().stream()
        .map(specialDto -> {
          Special special = Special.fromString(specialDto.getType());
          String customValue = special == Special.OTHER ? specialDto.getCustomValue() : null;
          return new WalkSpecial(special, customValue, savedWalk);
        })
        .collect(Collectors.toList());

    walkSpecialRepository.saveAll(specials);

    return new WalkCreateResponseDto(true, "산책로 등록에 성공했습니다.", savedWalk.getId());
  }

  public void deleteWalk(Long walkId, String token) {
    if (token == null || !jwtTokenProvider.validateToken(token)) {
      throw new IllegalArgumentException("잘못된 토큰입니다.");
    }

    String userEmail = jwtTokenProvider.getUserEmail(token);

    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Walk walk = walkRepository.findById(walkId)
        .orElseThrow(() -> new IllegalArgumentException("산책로를 찾을 수 없습니다."));

    if (!walk.getCreatedBy().equals(user)) {
      throw new RuntimeException("산책로 삭제 권한이 없습니다.");
    }

    walkRepository.delete(walk);
  }

  public WalkResponseDto searchWalks(String title) {
    List<Walk> walks = walkRepository.findByTitleContaining(title);

    List<WalkResponseDto.WalkDto> walkDtos = walks.stream()
        .map(walk -> {
          User createdByUser = walk.getCreatedBy();
          Pet pet = petRepository.findByUser(createdByUser);
          String breed = (pet != null) ? pet.getBreed() : null;

          return WalkResponseDto.WalkDto.builder()
              .walkId(walk.getId())
              .title(walk.getTitle())
              .description(walk.getDescription())
              .walkImageUrl(walk.getWalkImageUrl())
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