package DC_square.spring.repository.dday;

import DC_square.spring.domain.entity.Dday;
import DC_square.spring.domain.entity.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface DdayRepository extends JpaRepository<Dday, Long> {

  List<Dday> findAllByUser(User user);

  List<Dday> findAllByUserOrderByDayAsc(User user);

  @Query("select d from Dday d where d.isAlarm = true")
  List<Dday> findAllByIsAlarmTrue();
}