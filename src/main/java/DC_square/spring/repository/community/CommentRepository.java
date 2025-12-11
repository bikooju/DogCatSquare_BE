package DC_square.spring.repository.community;

import DC_square.spring.domain.entity.community.Comment;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommentRepository extends JpaRepository<Comment, Long> {

  // 특정 게시글의 최상위 댓글만 조회 (대댓글 제외)
  List<Comment> findByPostIdAndParentIsNull(Long postId);

  // 특정 게시글의 모든 댓글 조회 (대댓글 포함)
  List<Comment> findByPostId(Long postId);
}
