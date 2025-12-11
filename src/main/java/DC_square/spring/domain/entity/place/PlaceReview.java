package DC_square.spring.domain.entity.place;

import DC_square.spring.domain.entity.User;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@Table(name = "place_review")
@NoArgsConstructor
@AllArgsConstructor

public class PlaceReview {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "content", nullable = false, length = 100)
  private String content;

  @Column(name = "created_at", nullable = false)
  private LocalDateTime createdAt;

  @ElementCollection
  @CollectionTable(
      name = "place_review_images",
      joinColumns = @JoinColumn(name = "review_id")
  )
  @Column(name = "image_url")
  private List<String> placeReviewImageUrl;


  @ManyToOne
  @JoinColumn(name = "place_id", nullable = false)
  private Place place;

  @ManyToOne
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

}
