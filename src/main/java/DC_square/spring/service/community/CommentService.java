package DC_square.spring.service.community;

import DC_square.spring.domain.entity.Pet;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.community.Comment;
import DC_square.spring.domain.entity.community.Post;
import DC_square.spring.domain.enums.NotificationType;
import DC_square.spring.repository.community.CommentRepository;
import DC_square.spring.repository.community.PostRepository;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.service.notification.NotificationService;
import DC_square.spring.web.dto.request.community.CommentRequestDto;
import DC_square.spring.web.dto.response.community.CommentResponseDto;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CommentService {

  private final CommentRepository commentRepository;
  private final PostRepository postRepository;
  private final UserRepository userRepository;
  private final NotificationService notificationService;

  /**
   * 댓글 생성 API
   */
  public CommentResponseDto createComment(Long postId, Long userId,
      CommentRequestDto commentRequestDto) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    Comment comment = Comment.builder()
        .content(commentRequestDto.getContent())
        .post(post)
        .user(user)
        .replies(new ArrayList<>()) // replies 리스트 초기화
        .build();

    //대댓글인 경우
    if (commentRequestDto.getParentId() != null && !commentRequestDto.getParentId().toString()
        .trim().isEmpty()) {
      Comment parentComment = commentRepository.findById(commentRequestDto.getParentId())
          .orElseThrow(() -> new RuntimeException("부모 댓글을 찾을 수 없습니다."));

      // 대댓글의 대댓글 방지
      if (parentComment.getParent() != null) {
        throw new RuntimeException("대댓글에는 댓글을 작성할 수 없습니다.");
      }

      comment.setParent(parentComment);
    }

    Comment savedComment = commentRepository.save(comment);

    //게시글의 댓글 수 증가
    post.setCommentCount(post.getCommentCount() + 1);
    postRepository.save(post);

    sendCommentNotification(user, post, commentRequestDto.getContent());

    return convertToDto(savedComment, user);
  }

  private void sendCommentNotification(User commentWriter, Post post, String commentContent) {
    String body =
        commentWriter.getNickname() + "님이 [" + post.getTitle() + "]에 댓글을 남겼습니다. (postId : "
            + post.getId() + ")" + "\n" +
            commentContent;

    notificationService.sendNotificationAndSave(
        NotificationType.COMMENT,
        post.getUser(),
        body
    );
  }

  /**
   * 게시글의 모든 댓글 조회 (대댓글 포함)
   */
  public List<CommentResponseDto> getComments(Long postId) {
    // 게시글 존재 여부 확인
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("게시글을 찾을 수 없습니다."));

    /**
     * parent가 null인 댓글들만 먼저 가져옴 (댓글 1, 댓글 2)
     * 각 댓글의 대댓글은 @OneToMany 관계를 통해 필요할 때 조회됨
     */
    // 최상위 댓글만 먼저 조회
    List<Comment> comments = commentRepository.findByPostIdAndParentIsNull(postId);

    return comments.stream()
        .map(comment -> convertToDto(comment, comment.getUser())) // 댓글 작성자의 정보를 사용
        .collect(Collectors.toList());
  }


  /**
   * 댓글 삭제 API (해당 게시글에 속하는 댓글인지 확인 후 삭제)
   */
  @Transactional
  public void deleteComment(Long postId, Long commentId, Long userId) {

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    // 댓글 조회 (postId도 함께 검증)
    Comment comment = commentRepository.findById(commentId)
        .orElseThrow(() -> new RuntimeException("댓글을 찾을 수 없습니다."));

    // 댓글이 해당 게시글에 속하는지 확인
    if (!comment.getPost().getId().equals(postId)) {
      throw new RuntimeException("해당 게시글의 댓글이 아닙니다.");
    }

    //본인이 작성한 댓글인지 확인
    if (!comment.getUser().getId().equals(userId)) {
      throw new RuntimeException("본인이 작성한 댓글만 삭제할 수 있습니다.");
    }

    Post post = comment.getPost();

    // 댓글과 대댓글 삭제
    commentRepository.delete(comment);

    // 게시글의 댓글 수 감소 (대댓글 포함하여 전체 감소)
    int totalDeleted = countRepliesIncludingSelf(comment);
    post.setCommentCount(post.getCommentCount() - totalDeleted);
    postRepository.save(post);
  }

  /**
   * 해당 댓글과 그 대댓글 수를 포함한 총 삭제할 댓글 개수를 반환
   */
  private int countRepliesIncludingSelf(Comment comment) {
    int count = 1; // 자기 자신 포함
    if (comment.getReplies() != null) {
      for (Comment reply : comment.getReplies()) {
        count += countRepliesIncludingSelf(reply);
      }
    }
    return count;
  }


  private CommentResponseDto convertToDto(Comment comment, User user) {
    List<Pet> pets = user.getPetList(); // 사용자로부터 반려동물 목록을 가져옴
    String animalType = (pets.isEmpty()) ? "알 수 없음" : pets.get(0).getBreed(); // 첫 번째 반려동물의 품종

    // 대댓글 처리를 위해 각 대댓글에 대한 DTO 변환 (null 체크 추가)
    List<CommentResponseDto> replies = (comment.getReplies() == null) ? new ArrayList<>() :
        comment.getReplies().stream()
            .map(reply -> convertToDto(reply, reply.getUser())) //대댓글 작성자의 정보를 사용
            .collect(Collectors.toList());

    return CommentResponseDto.builder()
        .id(comment.getId())
        .userId(user.getId())
        .content(comment.getContent())
        .name(comment.getUser().getNickname())
        .created_at(LocalDateTime.now())
        .profileImage_URL(user.getProfileImageUrl())
        .replies(replies) // 대댓글 리스트 포함
        .animal_type(animalType)
        .build();
  }
}
