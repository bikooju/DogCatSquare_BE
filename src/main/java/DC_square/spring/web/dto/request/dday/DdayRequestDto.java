package DC_square.spring.web.dto.request.dday;

import DC_square.spring.domain.entity.Dday;
import DC_square.spring.domain.entity.User;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DdayRequestDto {

  private String title;
  private String day;  // "2024.02.13" 또는 "2024. 02. 13" 또는 "2024-02-13" 형식
  private Integer term;

  public Dday toEntity(User user) {
    try {
      // 공백이 있는 형식 시도 (yyyy. MM. dd)
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");
      LocalDate parsedDate = LocalDate.parse(this.day, formatter);
      return buildDday(user, parsedDate);
    } catch (DateTimeParseException e1) {
      try {
        // 하이픈 형식 시도 (yyyy-MM-dd)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        LocalDate parsedDate = LocalDate.parse(this.day, formatter);
        return buildDday(user, parsedDate);
      } catch (DateTimeParseException e2) {
        try {
          // 점 형식 시도 (yyyy.MM.dd)
          String formattedDate = day.replace(".", "-");
          return buildDday(user, LocalDate.parse(formattedDate));
        } catch (DateTimeParseException e3) {
          throw new RuntimeException(
              "날짜 형식이 올바르지 않습니다. (예: 2024.02.13 또는 2024. 02. 13 또는 2024-02-13)");
        }
      }
    }
  }

  private Dday buildDday(User user, LocalDate date) {
    return Dday.builder()
        .title(this.title)
        .day(date)
        .term(this.term)
        .user(user)
        .build();
  }
}