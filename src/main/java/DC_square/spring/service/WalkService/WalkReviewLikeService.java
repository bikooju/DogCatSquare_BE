package DC_square.spring.service.WalkService;

import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.walk.WalkReview;
import DC_square.spring.domain.entity.walk.WalkReviewLike;
import DC_square.spring.repository.WalkRepository.WalkReviewLikeRepository;
import DC_square.spring.repository.WalkRepository.WalkReviewRepository;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.web.dto.request.walk.WalkReviewLikeRequestDto;
import DC_square.spring.web.dto.response.walk.WalkReviewLikeResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class WalkReviewLikeService {

  private final WalkReviewLikeRepository walkReviewLikeRepository;
  private final WalkReviewRepository walkReviewRepository;
  private final UserRepository userRepository;


  public WalkReviewLikeResponseDto likeWalkReview(Long walkReviewId,
      WalkReviewLikeRequestDto requestDto) {
    User user = userRepository.findById(requestDto.getUserId())
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    WalkReview walkReview = walkReviewRepository.findById(walkReviewId)
        .orElseThrow(() -> new RuntimeException("산책로 후기를 찾을 수 없습니다."));

    if (walkReviewLikeRepository.existsByUserIdAndWalkReviewId(requestDto.getUserId(),
        walkReviewId)) {
      throw new RuntimeException("이미 좋아요를 누른 상태입니다.");
    }

    WalkReviewLike walkReviewLike = WalkReviewLike.builder()
        .user(user)
        .walkReview(walkReview)
        .isLiked(true)
        .build();

    walkReviewLikeRepository.save(walkReviewLike);

    return WalkReviewLikeResponseDto.builder()
        .status(200)
        .success(true)
        .message("좋아요를 추가했습니다.")
        .build();
  }

  public WalkReviewLikeResponseDto cancelLike(Long likeId) {
    WalkReviewLike walkReviewLike = walkReviewLikeRepository.findById(likeId)
        .orElseThrow(() -> new RuntimeException("좋아요 정보를 찾을 수 없습니다."));

    walkReviewLikeRepository.delete(walkReviewLike);

    return WalkReviewLikeResponseDto.builder()
        .status(200)
        .success(true)
        .message("좋아요를 취소했습니다.")
        .build();
  }
}