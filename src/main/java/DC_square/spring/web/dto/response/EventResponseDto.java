package DC_square.spring.web.dto.response;

import DC_square.spring.domain.entity.Event;
import java.time.format.DateTimeFormatter;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class EventResponseDto {

  private Long id;
  private String title;
  private String period;
  private String bannerImageUrl;
  private String eventUrl;

  public static EventResponseDto from(Event event) {
    String period = String.format("%s ~ %s",
        event.getStartDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")),
        event.getEndDate().format(DateTimeFormatter.ofPattern("yyyy.MM.dd"))
    );

    return EventResponseDto.builder()
        .id(event.getId())
        .title(event.getTitle())
        .period(period)
        .bannerImageUrl(event.getBannerImageUrl())
        .eventUrl(event.getEventUrl())
        .build();
  }
}