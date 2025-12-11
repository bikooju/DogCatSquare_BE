package DC_square.spring.domain.entity.walk;

import DC_square.spring.domain.entity.User;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
public class WalkReviewLike {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "user_id", nullable = false)
  private User user;

  @ManyToOne
  @JoinColumn(name = "walk_review_id")
  private WalkReview walkReview;

  private boolean isLiked;

  public WalkReviewLike(Long id, User user, WalkReview walkReview, boolean isLiked) {
    this.id = id;
    this.user = user;
    this.walkReview = walkReview;
    this.isLiked = isLiked;
  }
}