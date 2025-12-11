package DC_square.spring.domain.entity.community;

import DC_square.spring.domain.enums.BoardType;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.CreatedDate;

@Entity
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Board {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "board_id")
  private Long id;

  @Enumerated(EnumType.STRING)
  @Column(name = "board_type", nullable = false)
  private BoardType boardType;

  @Column(name = "content", nullable = false)
  private String content;

  @OneToMany(mappedBy = "board", cascade = CascadeType.ALL, orphanRemoval = true)
  private List<Keyword> keywordList = new ArrayList<>();
  //orphanRemoval = true : board.getKeywordList().clear() 호출 시, 기존 키워드가 고아 객체로 간주되어 DB에서 삭제됩니다.

  @CreatedDate
  private LocalDateTime createdDate = LocalDateTime.now();


}
