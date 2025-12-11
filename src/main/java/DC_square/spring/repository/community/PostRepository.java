package DC_square.spring.repository.community;

import DC_square.spring.domain.entity.community.Post;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;


public interface PostRepository extends JpaRepository<Post, Long> {

  List<Post> findByBoardId(Long boardId);

  List<Post> findByUserId(Long userId);

  //좋아요가 10개 이상인 게시글만 가져오고, 좋아요 순으로 내림차순 정렬
  List<Post> findByLikeCountGreaterThanEqualOrderByLikeCountDesc(int likeCount);
}
