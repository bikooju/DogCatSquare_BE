package DC_square.spring.config.jwt;

import DC_square.spring.apiPayload.ApiResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Collections;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.filter.OncePerRequestFilter;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

  private final JwtTokenProvider jwtTokenProvider;
  private final ObjectMapper objectMapper = new ObjectMapper();

  @Override
  protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
      FilterChain filterChain)
      throws ServletException, IOException {
    String token = jwtTokenProvider.resolveToken(request);

    try {
      if (token != null) {
        if (jwtTokenProvider.validateToken(token)) {
          String userEmail = jwtTokenProvider.getUserEmail(token);
          setAuthenticationToContext(userEmail);
        } else {
          // 토큰이 유효하지 않을 때 401 응답
          setErrorResponse(response, "TOKEN401", "액세스 토큰이 만료되었습니다.");
          return;
        }
      }
      filterChain.doFilter(request, response);
    } catch (Exception e) {
      // 그 외 토큰 관련 예외 발생 시 401 응답
      setErrorResponse(response, "TOKEN401", "유효하지 않은 토큰입니다.");
    }
  }

  private void setErrorResponse(HttpServletResponse response, String code, String message)
      throws IOException {
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.setContentType(MediaType.APPLICATION_JSON_VALUE);
    response.setCharacterEncoding("UTF-8");

    ApiResponse<Object> errorResponse = ApiResponse.onFailure(code, message, null);
    String jsonResponse = objectMapper.writeValueAsString(errorResponse);
    response.getWriter().write(jsonResponse);
  }

  private void setAuthenticationToContext(String userEmail) {
    User userDetails = new User(userEmail, "",
        Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));

    Authentication authentication = new UsernamePasswordAuthenticationToken(
        userDetails, "", userDetails.getAuthorities());

    SecurityContextHolder.getContext().setAuthentication(authentication);
  }
}