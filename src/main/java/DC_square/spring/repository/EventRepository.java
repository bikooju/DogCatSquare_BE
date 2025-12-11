package DC_square.spring.repository;

import DC_square.spring.domain.entity.Event;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

  // 종료일이 오늘 이후인 이벤트들을 시작일 기준으로 정렬하여 조회
  List<Event> findByEndDateGreaterThanEqualOrderByStartDateAsc(LocalDate today);
}