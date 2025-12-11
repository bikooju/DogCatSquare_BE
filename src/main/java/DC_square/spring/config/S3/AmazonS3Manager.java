package DC_square.spring.config.S3;

import DC_square.spring.config.AmazonConfig;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Component
@RequiredArgsConstructor
public class AmazonS3Manager {

  private final AmazonS3 amazonS3;

  private final AmazonConfig amazonConfig;

  private final UuidRepository uuidRepository;

  public String uploadFile(String keyName, MultipartFile file) {
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentType(file.getContentType());
    metadata.setContentLength(file.getSize());
    try {
      amazonS3.putObject(
          new PutObjectRequest(amazonConfig.getBucket(), keyName, file.getInputStream(), metadata));
    } catch (IOException e) {
      log.error("error at AmazonS3Manager uploadFile : {}", (Object) e.getStackTrace());
    }

    return amazonS3.getUrl(amazonConfig.getBucket(), keyName).toString();
  }

  public void deleteObjectByUrl(String url) {
    try {
      // 버킷 이름 추출
      String bucket = amazonConfig.getBucket();

      String key = extractKeyFromUrl(url);

      // key가 비정상일 경우 로그 출력
      if (key == null || key.isBlank()) {
        log.warn("S3 삭제 실패: key 추출 불가 (url: {})", url);
        return;
      }

      // 삭제
      amazonS3.deleteObject(bucket, key);
    } catch (Exception e) {
      log.error("S3 객체 삭제 중 오류 발생: {}", url, e);
    }
  }

  private String extractKeyFromUrl(String fileUrl) {
    try {
      // URI로 파싱
      java.net.URI uri = java.net.URI.create(fileUrl);
      String path = uri.getPath(); // "/community/uuid123"
      if (path == null || path.isBlank()) {
        return null;
      }

      // 앞의 "/" 제거 후 반환
      return path.startsWith("/") ? path.substring(1) : path;
    } catch (Exception e) {
      log.error("S3 URL 파싱 오류: {}", fileUrl, e);
      return null;
    }
  }

  public String generateProfile(Uuid uuid) {
    return amazonConfig.getProfilePath() + '/' + uuid.getUuid();
  }

  public String generatePet(Uuid uuid) {
    return amazonConfig.getPetPath() + '/' + uuid.getUuid();
  }

  public String generateReview(Uuid uuid) {
    return amazonConfig.getReviewPath() + '/' + uuid.getUuid();
  }

  public String generateWalk(Uuid uuid) {
    return amazonConfig.getWalkPath() + '/' + uuid.getUuid();
  }

  public String generateCommunity(Uuid uuid) {
    return amazonConfig.getCommunityPath() + '/' + uuid.getUuid();
  }

  public String generatedday(Uuid uuid) {
    return amazonConfig.getDdayPath() + '/' + uuid.getUuid();
  }

  public String generateWeather(Uuid uuid) {
    return amazonConfig.getWeatherPath() + '/' + uuid.getUuid();
  }
}
