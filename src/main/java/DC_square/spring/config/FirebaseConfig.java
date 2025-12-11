package DC_square.spring.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class FirebaseConfig implements InitializingBean {

  @Value("${app.fcm.service-account-json}") // yml에서 JSON 문자열을 그대로 읽음
  private String serviceAccountJson;

  @Override
  public void afterPropertiesSet() throws Exception {
    // 이미 초기화된 FirebaseApp 이 있으면 재초기화 방식
    if (!FirebaseApp.getApps().isEmpty()) {
      return;
    }

    // 필수: JSON 문자열이 없으면 오류
    if (serviceAccountJson == null || serviceAccountJson.isBlank()) {
      throw new IllegalArgumentException("FCM 서비스 계정 JSON이 제공되지 않았습니다.");
    }

    // JSON 문자열 -> InputStream -> GoogleCredentials
    ByteArrayInputStream in = new ByteArrayInputStream(
        serviceAccountJson.getBytes(StandardCharsets.UTF_8));
    GoogleCredentials credentials = GoogleCredentials.fromStream(in);

    // Firebase 옵션에 자격증명 주입
    FirebaseOptions options = FirebaseOptions.builder()
        .setCredentials(credentials)
        .build();

    // Firebase 초기화
    FirebaseApp.initializeApp(options);
    log.info("FirebaseApp initialized. apps={}", List.of(FirebaseApp.getApps()));
  }
}
