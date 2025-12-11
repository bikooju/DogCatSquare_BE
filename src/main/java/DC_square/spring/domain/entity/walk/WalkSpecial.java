package DC_square.spring.domain.entity.walk;

import DC_square.spring.domain.enums.Special;
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
public class WalkSpecial {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "special_type", nullable = false)
  private Special specialType;

  @Column(name = "custom_value")
  private String customValue;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "walk_id")
  private Walk walk;

  public WalkSpecial(Special specialType, String customValue, Walk walk) {
    this.specialType = specialType;
    this.customValue = customValue;
    this.walk = walk;
  }
}
