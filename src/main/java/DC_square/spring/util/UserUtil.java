package DC_square.spring.util;

import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.domain.entity.User;
import DC_square.spring.repository.community.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserUtil {

  private final JwtTokenProvider jwtTokenProvider;
  private final UserRepository userRepository;

  /**
   * HttpServletRequest에서 토큰을 꺼내 유저 엔티티 조회
   */
  public User getCurrentUser(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    String email = jwtTokenProvider.getUserEmail(token);

    return userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("해당 유저를 찾을 수 없습니다."));
  }

  /**
   * HttpServletRequest에서 토큰을 꺼내 userId만 가져오기
   */
  public Long getCurrentUserId(HttpServletRequest request) {
    return getCurrentUser(request).getId();
  }

  /**
   * HttpServletRequest에서 토큰을 꺼내 email만 가져오기
   */
  public String getCurrentUserEmail(HttpServletRequest request) {
    String token = jwtTokenProvider.resolveToken(request);
    return jwtTokenProvider.getUserEmail(token);
  }
}