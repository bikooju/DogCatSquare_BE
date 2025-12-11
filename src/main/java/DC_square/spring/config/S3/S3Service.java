package DC_square.spring.config.S3;

import DC_square.spring.config.AmazonConfig;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class S3Service {

  private final AmazonS3 amazonS3;
  private final AmazonConfig amazonConfig;

  public String uploadFile(String KeyName, MultipartFile file) throws IOException {
    System.out.println(KeyName);
    ObjectMetadata metadata = new ObjectMetadata();
    metadata.setContentLength(file.getSize());
    amazonS3.putObject(
        new PutObjectRequest(amazonConfig.getBucket(), KeyName, file.getInputStream(), metadata));
    return amazonS3.getUrl(amazonConfig.getBucket(), KeyName).toString();
  }
}