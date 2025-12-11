package DC_square.spring.service.place;

import DC_square.spring.config.S3.AmazonS3Manager;
import DC_square.spring.config.S3.Uuid;
import DC_square.spring.config.S3.UuidRepository;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.place.Place;
import DC_square.spring.domain.entity.place.PlaceReview;
//import DC_square.spring.domain.entity.place.PlaceReviewImage;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.repository.place.PlaceRepository;
import DC_square.spring.repository.place.PlaceReviewLikeRepository;
import DC_square.spring.repository.place.PlaceReviewRepository;
import DC_square.spring.web.dto.request.place.PlaceReviewCreateRequestDTO;
import DC_square.spring.web.dto.response.place.PlacePageResponseDTO;
import DC_square.spring.web.dto.response.place.PlaceReviewResponseDTO;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PlaceReviewService {

  private final PlaceReviewRepository placeReviewRepository;
  private final PlaceRepository placeRepository;
  private final UserRepository userRepository;
  private final PlaceReviewLikeRepository placeReviewLikeRepository;
  private final AmazonS3Manager s3Manager;
  private final UuidRepository uuidRepository;
  private final JwtTokenProvider jwtTokenProvider;
  private final PlaceReviewReportService placeReviewReportService;

  public Long createPlaceReview(PlaceReviewCreateRequestDTO request, Long placeId,
      List<MultipartFile> images, String token) {

    if (images.isEmpty()) {
      throw new RuntimeException("후기 이미지는 필수 입니다.");
    }

    String userEmail = jwtTokenProvider.getUserEmail(token);
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

    List<String> imageUrls = images.stream()
        .map(image -> {
          String uuid = UUID.randomUUID().toString();
          Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
          return s3Manager.uploadFile(s3Manager.generateReview(savedUuid), image);
        })
        .collect(Collectors.toList());

    Place place = placeRepository.findById(placeId)
        .orElseThrow(() -> new RuntimeException("장소를 찾을 수 없습니다."));

    PlaceReview placeReview = PlaceReview.builder()
        .user(user)
        .place(place)
        .content(request.getContent())
        .createdAt(ZonedDateTime.now(ZoneId.of("Asia/Seoul")).toLocalDateTime())
        .placeReviewImageUrl(imageUrls)
        .build();

    return placeReviewRepository.save(placeReview).getId();
  }

  public PlacePageResponseDTO<PlaceReviewResponseDTO> findPlaceReviews(Long placeId, String token,
      int page, int size) {
    List<PlaceReview> placeReviews = placeReviewRepository.findAllByPlaceId(placeId);

    //신고 기능을 위한 코드
    Long currentUserId = null;
    List<Long> reportedReviewIds = new ArrayList<>();
    List<Long> frequentlyReportedUserIds = new ArrayList<>();

    if (token != null) {
      String userEmail = jwtTokenProvider.getUserEmail(token);
      User currentUser = userRepository.findByEmail(userEmail).orElse(null);

      if (currentUser != null) {
        currentUserId = currentUser.getId();
        //사용자가 신고한 리뷰 ID 목록 가져오기
        reportedReviewIds = placeReviewReportService.getReportedReviewIds(token);
        //4회 이상 신고된 유저 ID 목록 가져오기
        frequentlyReportedUserIds = placeReviewReportService.getFrequentlyReportedUserIds();

      }
    }

    //
    final List<Long> finalReportedReviewIds = reportedReviewIds;
    final List<Long> finalFrequentlyReportedUserIds = frequentlyReportedUserIds;

    List<PlaceReviewResponseDTO> responseDTOs = placeReviews.stream()
        .filter(review -> !finalReportedReviewIds.contains(review.getId()))
        .filter(review -> !finalFrequentlyReportedUserIds.contains(review.getUser().getId()))
        .map(placeReview -> PlaceReviewResponseDTO.builder()
            .id(placeReview.getId())
            .breed(placeReview.getUser().getPetList().get(0).getBreed())
            .content(placeReview.getContent())
            //.isLiked(placeReviewLikeRepository.existsByUserIdAndPlaceReviewId(placeReview.getId(), placeReview.getUser().getId()))
            .userId(placeReview.getUser().getId())
            .nickname(placeReview.getUser().getNickname())
            .userImageUrl(placeReview.getUser().getProfileImageUrl())
            .createdAt(placeReview.getCreatedAt().toString())
            .placeReviewImageUrl(placeReview.getPlaceReviewImageUrl())
            .placeId(placeReview.getPlace().getId())
            .build())
        .collect(Collectors.toList());

    return PlacePageResponseDTO.of(responseDTOs, page, size);

//        return placeReviews.stream()
//                .map(placeReview -> PlaceReviewResponseDTO.builder()
//                        .id(placeReview.getId())
//                        .breed(placeReview.getUser().getPetList().get(0).getBreed())
//                        .content(placeReview.getContent())
//                        //.isLiked(placeReviewLikeRepository.existsByUserIdAndPlaceReviewId(placeReview.getId(), placeReview.getUser().getId()))
//                        .userId(placeReview.getUser().getId())
//                        .nickname(placeReview.getUser().getNickname())
//                        .userImageUrl(placeReview.getUser().getProfileImageUrl())
//                        .createdAt(placeReview.getCreatedAt().toString())
//                        .placeReviewImageUrl(placeReview.getPlaceReviewImageUrl())
//                        .placeId(placeReview.getPlace().getId())
//                        .build())
//                .collect(Collectors.toList());
  }

  public void deletePlaceReview(Long placeId, Long reviewId, String token) {

    String userEmail = jwtTokenProvider.getUserEmail(token);
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new RuntimeException("유저를 찾을 수 없습니다."));

    PlaceReview review = placeReviewRepository.findById(reviewId)
        .orElseThrow(() -> new RuntimeException("리뷰를 찾을 수 없습니다."));

    if (!review.getUser().getId().equals(user.getId())) {
      throw new RuntimeException("삭제할 권한이 없습니다.");
    }

    placeReviewRepository.deleteById(reviewId);
  }
}
