package DC_square.spring.web.controller.community;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.service.community.CommentService;
import DC_square.spring.web.dto.request.community.CommentRequestDto;
import DC_square.spring.web.dto.response.community.CommentResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments/posts/{postId}")
@Tag(name = "Comment API", description = "댓글 API")
public class CommentController {

  private final CommentService commentService;

  /**
   * 댓글 생성 API
   */
  @Operation(summary = "댓글 생성 API", description = "게시글id, 유저id를 꼭 넣어주세요")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @PostMapping("/users/{userId}")
  public ApiResponse<CommentResponseDto> createComment(@PathVariable Long postId,
      @PathVariable Long userId,
      @Valid @RequestBody CommentRequestDto commentRequestDto) {
    return ApiResponse.onSuccess(commentService.createComment(postId, userId, commentRequestDto));
  }

  /**
   * 특정 게시물의 댓글 조회 API
   */
  @Operation(summary = "댓글 조회 API", description = "게시글id를 꼭 넣어주세요")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @GetMapping
  public ApiResponse<List<CommentResponseDto>> getComments(@PathVariable Long postId) {
    List<CommentResponseDto> comments = commentService.getComments(postId);
    return ApiResponse.onSuccess(comments);
  }

  /**
   * 댓글 삭제 API (특정 게시글 내에서 삭제)
   */
  @Operation(summary = "댓글 삭제 API", description = "특정 게시글의 댓글을 삭제합니다. (대댓글 포함)")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @DeleteMapping("/{commentId}/users/{userId}")
  public ApiResponse<Void> deleteComment(@PathVariable Long postId, @PathVariable Long commentId,
      @PathVariable Long userId) {
    commentService.deleteComment(postId, commentId, userId);
    return ApiResponse.onSuccess(null);
  }
}
