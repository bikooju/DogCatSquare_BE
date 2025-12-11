package DC_square.spring.web.dto.request.dday;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DdayUpdateRequestDto {

  //private String title;
  // yyyy.mm.dd, yyyy. mm. dd, yyyy-mm-dd 형식 다 가능
  private String day;
  private Integer term;
  private Boolean isAlarm;

  public LocalDate parseDay() {
    try {
      // (yyyy. MM. dd)
      DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy. MM. dd");
      return LocalDate.parse(this.day, formatter);
    } catch (DateTimeParseException e1) {
      try {
        // (yyyy-MM-dd)
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        return LocalDate.parse(this.day, formatter);
      } catch (DateTimeParseException e2) {
        try {
          //  (yyyy.MM.dd)
          String formattedDate = day.replace(".", "-");
          return LocalDate.parse(formattedDate);
        } catch (DateTimeParseException e3) {
          throw new RuntimeException(
              "날짜 형식이 올바르지 않습니다. (예: 2024.02.13 또는 2024. 02. 13 또는 2024-02-13)");
        }
      }
    }
  }
}