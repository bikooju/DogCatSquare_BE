package DC_square.spring.web.controller;

import DC_square.spring.apiPayload.ApiResponse;
import DC_square.spring.service.EventService;
import DC_square.spring.web.dto.request.EventCreateRequestDto;
import DC_square.spring.web.dto.response.EventResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Tag(name = "Event", description = "이벤트 관련 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

  private final EventService eventService;

  @Operation(summary = "이벤트 등록 API", description = "새로운 이벤트를 등록합니다.")
  @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
  public ApiResponse<EventResponseDto> createEvent(
      @Valid @RequestPart("request") EventCreateRequestDto request,
      @RequestPart(value = "bannerImage", required = false) MultipartFile bannerImage
  ) {
    return ApiResponse.onSuccess(eventService.createEvent(request, bannerImage));
  }

  @Operation(summary = "전국 반려동물 이벤트 조회 API", description = "현재 진행 중인 이벤트 목록을 조회합니다.")
  @GetMapping
  public ApiResponse<List<EventResponseDto>> getEvents() {
    return ApiResponse.onSuccess(eventService.getEvents());
  }

  @Operation(summary = "이벤트 삭제 API", description = "특정 이벤트를 삭제합니다.")
  @DeleteMapping("/{eventId}")
  public ApiResponse<Void> deleteEvent(@PathVariable Long eventId) {
    eventService.deleteEvent(eventId);
    return ApiResponse.onSuccess(null);
  }

}