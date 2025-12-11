package DC_square.spring.repository.community;

import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.community.Post;
import DC_square.spring.domain.entity.community.PostLike;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

  boolean existsByPostAndUser(Post post, User user);
  //좋아요 여부 확인

  Optional<PostLike> findByPostAndUser(Post post, User user);
  //좋아요 데이터를 가져와서 삭제 또는 수정할 때

  List<PostLike> findAllByUser(User user);
}