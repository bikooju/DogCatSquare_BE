package DC_square.spring.web.controller.community;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.service.community.BoardService;
import DC_square.spring.web.dto.request.community.BoardRequestDto;
import DC_square.spring.web.dto.response.community.BoardResponseDto;
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
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/board")
@Tag(name = "Board API", description = "게시판 API")
public class BoardController {

  private final BoardService boardService;

  /**
   * 게시판 생성 API
   */
  @Operation(summary = "게시판 생성 API", description = "게시판 이름, 내용, 키워드(리스트 형태로) 작성해주세요")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @PostMapping
  public ApiResponse<BoardResponseDto> createBoard(
      @Valid @RequestBody BoardRequestDto boardRequestDto
  ) {
    return ApiResponse.onSuccess(boardService.createdBoard(boardRequestDto));
  }

  /**
   * 게시판 조회 API
   */
  @Operation(summary = "게시판 조회 API", description = "게시판 아이디를 입력해주세요")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @GetMapping("/{boardId}")
  public ApiResponse<BoardResponseDto> getBoard(@PathVariable Long boardId) {
    return ApiResponse.onSuccess(boardService.getBoard(boardId));
  }

  /**
   * 게시판 수정 API
   */
  @Operation(summary = "게시판 수정 API", description = "게시판 아이디를 입력해주세요")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @PutMapping("/{boardId}")
  public ApiResponse<BoardResponseDto> updateBoard(@PathVariable Long boardId,
      @Valid @RequestBody BoardRequestDto boardRequestDto) {
    return ApiResponse.onSuccess(boardService.updateBoard(boardId, boardRequestDto));
  }

  /**
   * 게시판 삭제 API
   */
  @Operation(summary = "게시판 삭제 API", description = "게시판 아이디를 입력해주세요")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @DeleteMapping("/{boardId}")
  public ApiResponse<Void> deleteBoard(@PathVariable Long boardId) {
    boardService.deleteBoard(boardId);
    return ApiResponse.onSuccess(null); //성공 응답 반환
  }

  /**
   * 게시판 조회 API
   */
  @Operation(summary = "게시판 검색 API", description = "게시판 이름을 입력해주세요")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @GetMapping("/search")
  public ApiResponse<List<BoardResponseDto>> searchBoard(@RequestParam String boardName) {
    if (boardName == null || boardName.trim().isEmpty()) {
      throw new IllegalArgumentException("게시글 제목은 필수입니다.");
    }

    List<BoardResponseDto> boardResponseDtoS = boardService.searchBoardByName(boardName);
    return ApiResponse.onSuccess(boardResponseDtoS);
  }

  /**
   * 게시판 전체 조회 API
   */
  @Operation(summary = "게시판 전체 조회 API", description = "모든 게시판 조회를 합니다.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "COMMON200", description = "OK, 성공")
  })
  @GetMapping("/all")
  public ApiResponse<List<BoardResponseDto>> getAllBoards() {
    List<BoardResponseDto> allBoards = boardService.getAllBoards();
    return ApiResponse.onSuccess(allBoards);
  }


}
