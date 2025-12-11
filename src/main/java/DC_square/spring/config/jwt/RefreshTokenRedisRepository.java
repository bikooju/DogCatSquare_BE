package DC_square.spring.config.jwt;

import java.time.Duration;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RefreshTokenRedisRepository {

  private final StringRedisTemplate redisTemplate;
  private static final String KEY_PREFIX = "RT:";
  private static final Duration REFRESH_TOKEN_DURATION = Duration.ofDays(14); // 2주

  public void save(String email, String refreshToken) {
    ValueOperations<String, String> values = redisTemplate.opsForValue();
    String key = getKey(email);
    values.set(key, refreshToken, REFRESH_TOKEN_DURATION);
  }

  public String findByEmail(String email) {
    ValueOperations<String, String> values = redisTemplate.opsForValue();
    String key = getKey(email);
    return values.get(key);
  }

  public void deleteByEmail(String email) {
    String key = getKey(email);
    redisTemplate.delete(key);
  }

  private String getKey(String email) {
    return KEY_PREFIX + email;
  }
}