package DC_square.spring.service.place;

import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.place.PlaceReview;
import DC_square.spring.domain.entity.place.ReviewReport;
import DC_square.spring.domain.enums.ReportType;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.repository.place.PlaceReviewRepository;
import DC_square.spring.repository.place.ReviewReportRepository;
import DC_square.spring.web.dto.request.place.ReviewReportRequestDTO;
import DC_square.spring.web.dto.response.place.ReviewReportResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PlaceReviewReportService {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;
  private final PlaceReviewRepository placereviewRepository;
  private final ReviewReportRepository reviewReportRepository;

  //리뷰 신고하기
  public Long reportReview(String token, Long reviewId, ReviewReportRequestDTO request) {

    String userEmail = jwtTokenProvider.getUserEmail(token);
    User reporter = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    PlaceReview review = placereviewRepository.findById(reviewId)
        .orElseThrow(() -> new IllegalArgumentException("리뷰를 찾을 수 없습니다."));

    User reportedUser = review.getUser();

    //자신의 리뷰인지 체크
    if (reporter.getId().equals(reportedUser.getId())) {
      throw new IllegalArgumentException("자신의 리뷰는 신고할 수 없습니다.");
    }

    //이미 신고한 리뷰인지 체크
    if (reviewReportRepository.existsByReporterIdAndReviewId(reporter.getId(), reviewId)) {
      throw new IllegalArgumentException("이미 신고한 리뷰입니다.");
    }

    //기타 선택 시 사유 입력 필수
    if (request.getReportType() == ReportType.OTHER) {
      if (request.getOtherReason() == null || request.getOtherReason().length() < 10
          || request.getOtherReason().length() > 50) {
        throw new IllegalArgumentException("기타 사유는 10자 이상 50자 이하로 입력해주세요.");
      }
    } else {
      // For non-OTHER types, otherReason should be null
      request.setOtherReason(null);
    }

    ReviewReport report = ReviewReport.builder()
        .reporter(reporter)
        .reportedUser(reportedUser)
        .review(review)
        .reportType(request.getReportType())
        .otherReason(request.getReportType() == ReportType.OTHER ? request.getOtherReason() : null)
        .build();

    return reviewReportRepository.save(report).getId();
  }

  //신고한 리뷰 ID 목록 조회 (해당 유저에게 가릴 리뷰 목록)
  public List<Long> getReportedReviewIds(String token) {
    String userEmail = jwtTokenProvider.getUserEmail(token);
    User user = userRepository.findByEmail(userEmail)
        .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

    return reviewReportRepository.findReportedReviewIdsByUserId(user.getId());
  }

  //4회 이상 신고된 유저 ID 목록 조회
  public List<Long> getFrequentlyReportedUserIds() {
    return reviewReportRepository.findUserIdsReportedFourOrMoreTimes();
  }

  // 특정 유저가 4회 이상 신고되었는지 확인
  public boolean isUserFrequentlyReported(Long userId) {
    return reviewReportRepository.isUserReportedFourOrMoreTimes(userId);
  }
}
