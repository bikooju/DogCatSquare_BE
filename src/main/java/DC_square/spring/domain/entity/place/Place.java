package DC_square.spring.domain.entity.place;

import DC_square.spring.domain.entity.region.City;
import DC_square.spring.domain.entity.region.Province;
import DC_square.spring.domain.enums.PlaceCategory;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "place")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Place {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY) // AUTO_INCREMENT
  private Long id;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "address", length = 100)
  private String address;

  @Enumerated(EnumType.STRING) // enum -> 문자열로 저장
  @Column(name = "category", nullable = false)
  private PlaceCategory category;

  @Column(name = "phone_number", length = 20)
  private String phoneNumber;

  //@Column(name = "open") // NULL 허용
  //private Boolean open;

  @Column(name = "view", nullable = false)
  private Integer view;

  // 조회수 초기값 0으로 설정
  @PrePersist
  public void prePersist() {
    this.view = (this.view == null) ? 0 : this.view;
    this.hidden = (this.hidden == null) ? false : this.hidden;
  }

  @Column(name = "google_place_id", unique = true)
  private String googlePlaceId;

  @Column(name = "hidden", nullable = false)
  private Boolean hidden;

//    @ManyToOne
//    @JoinColumn(name = "region_id", nullable = false)
//    private Region region;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "province_id")
  private Province province;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "city_id")
  private City city;

  @Column(name = "longitude", nullable = false) // -180.000000 ~ 180.000000
  private Double longitude;

  @Column(name = "latitude", nullable = false) // -180.000000 ~ 180.000000
  private Double latitude;

  @OneToOne(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
  private PlaceDetail placeDetail;

  @OneToMany(mappedBy = "place", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<PlaceImage> images = new ArrayList<>();

  @ElementCollection
  @CollectionTable(
      name = "place_keywords",
      joinColumns = @JoinColumn(name = "place_id")
  )
  @Column(name = "keyword")
  private List<String> keywords = new ArrayList<>();
}

