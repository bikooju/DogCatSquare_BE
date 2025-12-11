package DC_square.spring.service.WalkService;

import DC_square.spring.config.S3.AmazonS3Manager;
import DC_square.spring.config.S3.Uuid;
import DC_square.spring.config.S3.UuidRepository;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.domain.entity.Pet;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.walk.Walk;
import DC_square.spring.domain.entity.walk.WalkReview;
import DC_square.spring.repository.PetRepository;
import DC_square.spring.repository.WalkRepository.WalkRepository;
import DC_square.spring.repository.WalkRepository.WalkReviewRepository;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.web.dto.request.walk.WalkReviewCreateRequestDto;
import DC_square.spring.web.dto.response.walk.WalkResponseDto;
import DC_square.spring.web.dto.response.walk.WalkReviewResponseDto;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class WalkReviewService {

  private final WalkReviewRepository walkReviewRepository;
  private final WalkRepository walkRepository;
  private final UserRepository userRepository;
  private final PetRepository petRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final AmazonS3Manager s3Manager;
  private final UuidRepository uuidRepository;

  public WalkReviewResponseDto createWalkReview(WalkReviewCreateRequestDto request, Long walkId,
      String token, List<MultipartFile> images) {
    if (images.isEmpty()) {
      throw new RuntimeException("후기 이미지는 필수 입니다.");
    }

    List<String> imageUrls = images.stream()
        .map(image -> {
          String uuid = UUID.randomUUID().toString();
          Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
          return s3Manager.uploadFile(s3Manager.generateReview(savedUuid), image);
        })
        .collect(Collectors.toList());

    if (token == null || !jwtTokenProvider.validateToken(token)) {
      throw new IllegalArgumentException("잘못된 토큰입니다.");
    }

    String userEmail = jwtTokenProvider.getUserEmail(token);

    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    Walk walk = walkRepository.findById(walkId)
        .orElseThrow(() -> new RuntimeException("산책로를 찾을 수 없습니다."));

    Pet pet = petRepository.findByUser(user);

    String breed = (pet != null) ? pet.getBreed() : null;
    String profileImageUrl = user.getProfileImageUrl();

    WalkReview walkReview = WalkReview.builder()
        .user(user)
        .walk(walk)
        .content(request.getContent())
        .walkReviewImageUrl(imageUrls)
        .build();

    WalkReview savedWalkReview = walkReviewRepository.save(walkReview);

    int newReviewCount = walkReviewRepository.countByWalk(walk);
    walk.updateReviewCount(newReviewCount);
    walkRepository.save(walk);

    WalkReviewResponseDto.WalkReviewDto reviewDto = WalkReviewResponseDto.WalkReviewDto.builder()
        .reviewId(savedWalkReview.getId())
        .walkId(savedWalkReview.getWalk().getId())
        .content(savedWalkReview.getContent())
        .walkReviewImageUrl(savedWalkReview.getWalkReviewImageUrl())
        .createdAt(savedWalkReview.getCreatedAt())
        .updatedAt(savedWalkReview.getUpdatedAt())
        .createdBy(WalkResponseDto.CreatedByDto.builder()
            .nickname(savedWalkReview.getUser().getNickname())
            .profileImageUrl(profileImageUrl)
            .breed(breed)
            .build())
        .build();

    return new WalkReviewResponseDto(List.of(reviewDto));
  }

  public void deleteWalkReview(Long walkId, Long reviewId, String token) throws RuntimeException {
    if (token == null || !jwtTokenProvider.validateToken(token)) {
      throw new IllegalArgumentException("잘못된 토큰입니다.");
    }

    String userEmail = jwtTokenProvider.getUserEmail(token);

    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));

    WalkReview walkReview = walkReviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("해당 후기를 찾을 수 없습니다."));

    if (!walkReview.getWalk().getId().equals(walkId)) {
      throw new RuntimeException("해당 산책로에 존재하는 후기가 아닙니다.");
    }

    if (!walkReview.getUser().getEmail().equals(userEmail)) {
      throw new RuntimeException("산책로 후기 삭제 권한이 없습니다.");
    }

    Walk walk = walkReview.getWalk();

    walkReviewRepository.delete(walkReview);

    Walk updatedWalk = walkRepository.findById(walk.getId())
        .orElseThrow(() -> new RuntimeException("산책로를 찾을 수 없습니다."));

    int newReviewCount = walkReviewRepository.countByWalk(updatedWalk);
    updatedWalk.updateReviewCount(newReviewCount);

    walkRepository.save(updatedWalk);
  }


  public WalkReviewResponseDto viewWalkReviewList(Long walkId) {
    Walk walk = walkRepository.findById(walkId)
        .orElseThrow(() -> new RuntimeException("산책로를 찾을 수 없습니다."));

    List<WalkReview> walkReviews = walkReviewRepository.findByWalk(walk);

    List<WalkReviewResponseDto.WalkReviewDto> walkReviewDtos = walkReviews.stream()
        .map(review -> {
          Pet pet = petRepository.findByUser(review.getUser());
          String breed = (pet != null) ? pet.getBreed() : null;
          String profileImageUrl = review.getUser().getProfileImageUrl();

          return WalkReviewResponseDto.WalkReviewDto.builder()
              .reviewId(review.getId())
              .walkId(walk.getId())
              .content(review.getContent())
              .walkReviewImageUrl(review.getWalkReviewImageUrl())
              .createdAt(review.getCreatedAt())
              .updatedAt(review.getUpdatedAt())
              .createdBy(WalkResponseDto.CreatedByDto.builder()
                  .nickname(review.getUser().getNickname())
                  .profileImageUrl(profileImageUrl)
                  .breed(breed)
                  .build())
              .build();
        })
        .collect(Collectors.toList());

    return WalkReviewResponseDto.builder()
        .walkReviews(walkReviewDtos)
        .build();
  }
}