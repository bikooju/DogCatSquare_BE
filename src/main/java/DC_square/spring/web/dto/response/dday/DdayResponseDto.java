package DC_square.spring.web.dto.response.dday;


import DC_square.spring.domain.entity.Dday;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DdayResponseDto {

  private Long id;
  private String title;
  private String day;
  private Integer term;
  private Long daysLeft;
  private String dDayText;
  private String dDayImageUrl;
  private Boolean isAlarm;

  public static DdayResponseDto from(Dday dday) {
    LocalDate today = LocalDate.now();
    long daysUntil = ChronoUnit.DAYS.between(today, dday.getDay());
    String dDayStr = daysUntil > 0 ? "D-" + daysUntil :
        daysUntil == 0 ? "D-Day" :
            "D+" + Math.abs(daysUntil);

    return DdayResponseDto.builder()
        .id(dday.getId())
        .title(dday.getTitle())
        .day(dday.getDay().format(DateTimeFormatter.ofPattern("yyyy. MM. dd")))
        .term(dday.getTerm())
        .daysLeft(daysUntil)
        .dDayText(dDayStr)
        .dDayImageUrl(dday.getImageUrl())
        .isAlarm(dday.getIsAlarm())
        .build();
  }
}