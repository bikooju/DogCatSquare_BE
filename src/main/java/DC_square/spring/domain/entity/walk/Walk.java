package DC_square.spring.domain.entity.walk;

import DC_square.spring.domain.entity.Coordinate;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.enums.Difficulty;
import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Walk {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 20)
  private String title;

  @Lob
  @Column(nullable = false)
  private String description;

  @Column(nullable = false)
  @Builder.Default
  private Integer reviewCount = 0;

  @Column(nullable = false)
  private Double distance;

  private Integer time;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  private Difficulty difficulty;

  @OneToMany(mappedBy = "walk", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<WalkSpecial> specials = new ArrayList<>();

  @ElementCollection
  @CollectionTable(
      name = "walk_images",
      joinColumns = @JoinColumn(name = "walk_id")
  )
  @Column(name = "image_url")
  private List<String> walkImageUrl;

  @Builder.Default
  private LocalDateTime createdAt = LocalDateTime.now();

  @Builder.Default
  private LocalDateTime updatedAt = LocalDateTime.now();

  @ElementCollection
  @CollectionTable(name = "walk_coordinates",
      joinColumns = @JoinColumn(name = "walk_id"))
  private List<Coordinate> coordinates = new ArrayList<>();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id")
  @OnDelete(action = OnDeleteAction.CASCADE)
  private User createdBy;

  public void updateReviewCount(int count) {
    this.reviewCount = count;
  }
}