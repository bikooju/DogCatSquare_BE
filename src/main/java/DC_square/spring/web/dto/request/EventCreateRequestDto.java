package DC_square.spring.web.dto.request;

import DC_square.spring.domain.entity.Event;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EventCreateRequestDto {

  @NotBlank(message = "이벤트 제목은 필수입니다.")
  private String title;

  @NotNull(message = "시작 날짜는 필수입니다.")
  private String startDate;

  @NotNull(message = "종료 날짜는 필수입니다.")
  private String endDate;

  private String eventUrl;

  public Event toEntity(String bannerImageUrl) {
    return Event.builder()
        .title(title)
        .startDate(LocalDate.parse(startDate))
        .endDate(LocalDate.parse(endDate))
        .bannerImageUrl(bannerImageUrl)
        .eventUrl(eventUrl)
        .build();
  }
}