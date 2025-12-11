package DC_square.spring.web.dto.request.notification;

import jakarta.validation.constraints.Size;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자가 읽은(읽음 처리할) 알림 ID 목록을 서버에 전달할때 쓰는 요청 DTO
 */
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReadNotificationRequestDto {

  @Size(max = 100) // 상한 제한
  private List<Long> ids; // 비어있거나 null이면 전부 읽음 처리
}
