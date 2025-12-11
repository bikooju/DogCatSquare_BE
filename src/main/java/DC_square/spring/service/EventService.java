package DC_square.spring.service;

import DC_square.spring.config.S3.AmazonS3Manager;
import DC_square.spring.config.S3.Uuid;
import DC_square.spring.config.S3.UuidRepository;
import DC_square.spring.domain.entity.Event;
import DC_square.spring.repository.EventRepository;
import DC_square.spring.web.dto.request.EventCreateRequestDto;
import DC_square.spring.web.dto.response.EventResponseDto;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventService {

  private final EventRepository eventRepository;
  private final UuidRepository uuidRepository;
  private final AmazonS3Manager s3Manager;

  // 이벤트 등록
  @Transactional
  public EventResponseDto createEvent(EventCreateRequestDto request, MultipartFile bannerImage) {
    String bannerImageUrl = null;
    if (bannerImage != null && !bannerImage.isEmpty()) {
      String uuid = UUID.randomUUID().toString();
      Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
      bannerImageUrl = s3Manager.uploadFile(
          s3Manager.generateCommunity(savedUuid),
          bannerImage
      );
    }

    // 이벤트 생성 및 저장
    Event event = request.toEntity(bannerImageUrl);
    Event savedEvent = eventRepository.save(event);

    return EventResponseDto.from(savedEvent);
  }


  // 이벤트 조회
  public List<EventResponseDto> getEvents() {
    LocalDate today = LocalDate.now();

    // 현재 진행 중이거나 시작하지 않은 이벤트를 조회
    List<Event> events = eventRepository.findByEndDateGreaterThanEqualOrderByStartDateAsc(today);

    //event 엔티티 리스트를 EventResponseDto 리스트로 변환
    return events.stream()
        .map(EventResponseDto::from)
        .collect(Collectors.toList());
  }

  //이벤트 삭제
  @Transactional
  public void deleteEvent(Long eventId) {
    Event event = eventRepository.findById(eventId)
        .orElseThrow(() -> new RuntimeException("해당 이벤트를 찾을 수 없습니다."));
    //추후에 S3에서 이미지 삭제로직 추가
    eventRepository.delete(event);
  }
}