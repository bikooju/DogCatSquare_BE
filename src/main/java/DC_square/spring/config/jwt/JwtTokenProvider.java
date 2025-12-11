package DC_square.spring.config.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

  @Value("${jwt.secret}")
  private String secretKey;  // application.yml에서 설정한 키

  // 토큰 유효기간 설정 (24시간)
  // private final long tokenValidityInMilliseconds = 1000L * 60 * 60 * 24;

  //토큰 유효기간  설정
//    private final long accessTokenValidityInMilliseconds = 1000L * 60 * 1; // 2분
  private final long accessTokenValidityInMilliseconds = 1000L * 60 * 60 * 24; // 24시
  private final long refreshTokenValidityInMilliseconds = 1000L * 60 * 60 * 24 * 14; // 2주
//   private final long refreshTokenValidityInMilliseconds = 1000L * 60 * 5; // 5분

  private final RefreshTokenRedisRepository refreshTokenRedisRepository;


  // 객체 초기화시 비밀키를 Base64로 인코딩
  @PostConstruct
  protected void init() {
    secretKey = Base64.getEncoder().encodeToString(secretKey.getBytes());
  }

  // JWT 토큰 생성 메서드
  public String createAccessToken(String userEmail) {
    // 1. Claims 객체에 토큰에 담을 데이터 설정
    Claims claims = Jwts.claims().setSubject(userEmail);
    claims.put("role", "ROLE_USER");  // 사용자 역할 정보

    // 2. 토큰 생성 시간 설정
    Date now = new Date();
    Date validity = new Date(now.getTime() + accessTokenValidityInMilliseconds);

    // 3. JWT 토큰 생성
    return Jwts.builder()
        .setClaims(claims)  // 정보 담기
        .setIssuedAt(now)  // 토큰 발행 시간
        .setExpiration(validity)   // 토큰 만료 시간
        .signWith(SignatureAlgorithm.HS256, secretKey)  // 암호화 알고리즘, 비밀키
        .compact();
  }

  // Refresh Token 생성
  public String createRefreshToken(String userEmail) {
    Claims claims = Jwts.claims().setSubject(userEmail);
    Date now = new Date();
    Date validity = new Date(now.getTime() + refreshTokenValidityInMilliseconds);

    String refreshToken = Jwts.builder()
        .setClaims(claims) // 정보 담기
        .setIssuedAt(now)  // 토큰 발행 시간
        .setExpiration(validity)  // 토큰 만료 시간
        .signWith(SignatureAlgorithm.HS256, secretKey)  // 암호화 알고리즘, 비밀키
        .compact();

    // Redis에 저장
    refreshTokenRedisRepository.save(userEmail, refreshToken);

    return refreshToken;
  }

  // 토큰에서 값 추출
  public String getUserEmail(String token) {
    return Jwts.parser()
        .setSigningKey(secretKey)
        .parseClaimsJws(token)
        .getBody()
        .getSubject();
  }

  // Refresh Token 검증
  public boolean validateRefreshToken(String token) {
    try {
      Jws<Claims> claims = Jwts.parser()
          .setSigningKey(secretKey)
          .parseClaimsJws(token);

      String userEmail = claims.getBody().getSubject();
      String savedToken = refreshTokenRedisRepository.findByEmail(userEmail);

      if (savedToken == null || !savedToken.equals(token)) {
        return false;
      }

      return !claims.getBody()
          .getExpiration()
          .before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  // Access  Token 유효성 검증
  public boolean validateToken(String token) {
    try {
      Jws<Claims> claims = Jwts.parser()
          .setSigningKey(secretKey)
          .parseClaimsJws(token);

      // 토큰 만료시간 검증
      return !claims.getBody()
          .getExpiration()
          .before(new Date());
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  //헤더에서 토큰 추출
  public String resolveToken(HttpServletRequest request) {
    String bearerToken = request.getHeader("Authorization");
    if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
      return bearerToken.substring(7);
    }
    return null;
  }
}