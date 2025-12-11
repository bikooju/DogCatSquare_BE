package DC_square.spring.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Configuration
@ConfigurationProperties(prefix = "google.maps")
@Getter
@Setter
public class GoogleMapsConfig {

  @Value("${google.maps.api-key}")
  private String apiKey;

  @Value("${google.translate.api-key}")
  private String translateApiKey;
}
