package DC_square.spring.domain.entity;

import DC_square.spring.domain.enums.DdayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Dday {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String title;

  @Column(nullable = false)
  private LocalDate day;

  @Column
  private Integer term;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @Enumerated(EnumType.STRING)
  private DdayType type;

  @Column
  private String imageUrl;

  @Builder.Default
  @Column(nullable = false)
  private Boolean isAlarm = true;

  public void setDefaultImageUrl() {
    switch (this.type) {
      case FOOD:
        this.imageUrl = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/pet/e085b385-c8af-4e49-971c-5f89fa6f22da";
        break;
      case PAD:
        this.imageUrl = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/pet/cc948f41-584f-4cab-97bf-7c9b5cf02878";
        break;
      case HOSPITAL:
        this.imageUrl = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/pet/2527be55-bdff-4ffb-80a7-32496689b5c5";
        break;
      case CUSTOM:
        this.imageUrl = "https://dogcatsquare.s3.ap-northeast-2.amazonaws.com/pet/3057edc6-9efa-4147-ba88-6276f057ffbb";
        break;
    }
  }

  public long getDaysLeft() {
    return ChronoUnit.DAYS.between(LocalDate.now(), this.day);
  }
}