package DC_square.spring.domain.entity.place;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "placeDetail")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PlaceDetail {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "businessHours", columnDefinition = "TEXT")
  private String businessHours;

  @Column(name = "homepage_url")
  private String homepageUrl;

  @ElementCollection
  @Column(name = "facilities")
  private List<String> facilities = new ArrayList<>();

  @Column(name = "description")
  private String description;

  @OneToOne
  @JoinColumn(name = "place_id", nullable = false) // Place의 외래 키
  private Place place;

  @OneToMany(mappedBy = "placeDetail", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Cost> costs;

  @Column(name = "additional_info", length = 500)
  private String additionalInfo;
}
