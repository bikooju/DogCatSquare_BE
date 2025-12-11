package DC_square.spring.service.community;

import DC_square.spring.config.S3.AmazonS3Manager;
import DC_square.spring.config.S3.Uuid;
import DC_square.spring.config.S3.UuidRepository;
import DC_square.spring.domain.entity.Pet;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.community.Board;
import DC_square.spring.domain.entity.community.Post;
import DC_square.spring.repository.community.BoardRepository;
import DC_square.spring.repository.community.PostRepository;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.web.dto.request.community.PostRequestDto;
import DC_square.spring.web.dto.request.community.UpdatePostRequestDto;
import DC_square.spring.web.dto.response.community.PostResponseDto;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

//test
@Service
@RequiredArgsConstructor
public class PostService {

  private final PostRepository postRepository;
  private final BoardRepository boardRepository;
  private final UserRepository userRepository;

  private final UuidRepository uuidRepository;
  private final AmazonS3Manager s3Manager;


  /**
   * 게시글 생성 API
   */
  public PostResponseDto createPost(List<MultipartFile> images, PostRequestDto postRequestDto,
      Long userId) {

    // images가 null인 경우 빈 리스트로 초기화
    List<String> imageUrls = (images != null) ? images.stream()
        .map(image -> {
          String uuid = UUID.randomUUID().toString();
          Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
          return s3Manager.uploadFile(s3Manager.generateCommunity(savedUuid), image);
        })
        .collect(Collectors.toList()) : new ArrayList<>();

    // 유튜브 영상 ID 추출
    String thumbnailUrl = null;
    if (postRequestDto.getVideo_URL() != null && !postRequestDto.getVideo_URL().isEmpty()) {
      String videoId = postRequestDto.getVideo_URL()
          .substring(postRequestDto.getVideo_URL().length() - 11);
      thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
    }

    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("해당 사용자 조회할 수 없습니다."));

    Board findBoard = boardRepository.findByBoardType(postRequestDto.getBoardType());

    List<Pet> pets = user.getPetList(); // 사용자로부터 반려동물 목록을 가져옴
    String animalType = (pets.isEmpty()) ? "알 수 없음" : pets.get(0).getBreed(); // 첫 번째 반려동물의 품종

    //Post 엔티티
    Post post = Post.builder()
        .title(postRequestDto.getTitle())
        .content(postRequestDto.getContent())
        .likeCount(0) //초기 좋아요 수
        .commentCount(0) //초기 댓글 수
        .communityImages(imageUrls)
        .user(user)
        .communityImages(imageUrls)
        .created_at(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .video_URL(postRequestDto.getVideo_URL())
        .board(findBoard)
        .build();

    //저장
    Post savedPost = postRepository.save(post);

    //PostResponseDto로 반환
    return PostResponseDto.builder()
        .id(savedPost.getId())
        .boardType(savedPost.getBoard().getBoardType().getDisplayName())
        .title(savedPost.getTitle())
        .content(savedPost.getContent())
        .video_URL(savedPost.getVideo_URL())
        .images(savedPost.getCommunityImages())
        .animal_type(animalType)
        .like_count(savedPost.getLikeCount())
        .username(savedPost.getUser().getNickname())
        .comment_count(savedPost.getCommentCount())
        .thumbnail_URL(thumbnailUrl)
        .profileImage_URL(user.getProfileImageUrl())
        .createdAt(savedPost.getCreated_at())
        .updatedAt(savedPost.getUpdatedAt())
        .userId(savedPost.getUser().getId())
        .build();
  }

  /**
   * 특정 게시글 조회 API(한개 조회)
   */
  public PostResponseDto getPost(Long postId) {
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("해당 게시글이 존재하지 않습니다."));

    // 유튜브 영상 ID 추출
    String thumbnailUrl = null;
    if (post.getVideo_URL() != null && !post.getVideo_URL().isEmpty()) {
      String videoId = post.getVideo_URL().substring(post.getVideo_URL().length() - 11);
      thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
    }

    List<Pet> pets = post.getUser().getPetList(); // 사용자로부터 반려동물 목록을 가져옴
    String animalType = (pets.isEmpty()) ? "알 수 없음" : pets.get(0).getBreed(); // 첫 번째 반려동물의 품종

    return PostResponseDto.builder()
        .id(post.getId())
        .boardType(post.getBoard().getBoardType().getDisplayName())
        .title(post.getTitle())
        .content(post.getContent())
        .content(post.getContent())
        .animal_type(animalType)
        .video_URL(post.getVideo_URL())
        .username(post.getUser().getNickname())
        .thumbnail_URL(thumbnailUrl)
        .profileImage_URL(post.getUser().getProfileImageUrl())
        .images(post.getCommunityImages())
        .like_count(post.getLikeCount())
        .thumbnail_URL(post.getVideo_URL() + "/0.jpg")
        .comment_count(post.getCommentCount())
        .createdAt(post.getCreated_at())
        .updatedAt(post.getUpdatedAt())
        .userId(post.getUser().getId())
        .build();
  }

  /**
   * 게시판 id로 특정 게시판에 있는 게시글들 조회
   */
  public List<PostResponseDto> getPosts(Long boardId) {

    // 게시판 ID가 유효한지 확인
    Board board = boardRepository.findById(boardId)
        .orElseThrow(() -> new IllegalArgumentException("해당 게시판 ID가 존재하지 않습니다."));

    // 게시판 ID로 게시글들 조회
    List<Post> posts = postRepository.findByBoardId(boardId);

    // 게시글이 없으면 예외 처리
    if (posts.isEmpty()) {
      throw new IllegalArgumentException("해당 게시판에 게시글이 존재하지 않습니다.");
    }

    // PostResponseDto로 변환하여 반환
    return posts.stream()
        .map(post -> {
          // 유튜브 영상 ID 추출 (썸네일 URL 생성)
          String thumbnailUrl = null;
          if (post.getVideo_URL() != null && !post.getVideo_URL().isEmpty()) {
            String videoId = post.getVideo_URL().substring(post.getVideo_URL().length() - 11);
            thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
          }

          List<Pet> pets = post.getUser().getPetList(); // 사용자로부터 반려동물 목록을 가져옴
          String animalType = (pets.isEmpty()) ? "알 수 없음" : pets.get(0).getBreed(); // 첫 번째 반려동물의 품종

          return PostResponseDto.builder()
              .id(post.getId())
              .boardType(post.getBoard().getBoardType().getDisplayName())
              .title(post.getTitle())
              .content(post.getContent())
              .video_URL(post.getVideo_URL())
              .thumbnail_URL(thumbnailUrl)
              .animal_type(animalType)
              .username(post.getUser().getNickname())
              .profileImage_URL(post.getUser().getProfileImageUrl())
              .images(post.getCommunityImages())
              .like_count(post.getLikeCount())
              .comment_count(post.getCommentCount())
              .createdAt(post.getCreated_at()) // 게시글 생성일
              .updatedAt(post.getUpdatedAt())
              .userId(post.getUser().getId())
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * 사용자가 작성한 모든 게시글 조회
   */
  public List<PostResponseDto> getPostsByUser(Long userId) {
    //사용자가 존재하는지 확인
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new IllegalArgumentException("해당 사용자를 찾을 수 없습니다."));

    //사용자가 작성한 게시글들 조회
    List<Post> posts = postRepository.findByUserId(userId);

    // 게시글이 없으면 예외 처리
    if (posts.isEmpty()) {
      throw new RuntimeException("사용자가 작성한 게시글이 존재하지 않습니다.");
    }

    // 게시글들을 PostResponseDto로 변환하여 반환
    return posts.stream()
        .map(post -> {
          // 유튜브 영상 ID 추출 (썸네일 URL 생성)
          String thumbnailUrl = null;
          if (post.getVideo_URL() != null && !post.getVideo_URL().isEmpty()) {
            String videoId = post.getVideo_URL().substring(post.getVideo_URL().length() - 11);
            thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
          }

          List<Pet> pets = post.getUser().getPetList(); // 사용자로부터 반려동물 목록을 가져옴
          String animalType = (pets.isEmpty()) ? "알 수 없음" : pets.get(0).getBreed(); // 첫 번째 반려동물의 품종

          return PostResponseDto.builder()
              .id(post.getId())
              .boardType(post.getBoard().getBoardType().getDisplayName()) // 게시판 이름
              .title(post.getTitle())
              .content(post.getContent())
              .video_URL(post.getVideo_URL())
              .thumbnail_URL(thumbnailUrl)
              .animal_type(animalType)
              .username(post.getUser().getNickname())
              .profileImage_URL(post.getUser().getProfileImageUrl())
              .images(post.getCommunityImages())
              .like_count(post.getLikeCount())
              .comment_count(post.getCommentCount())
              .createdAt(post.getCreated_at()) // 게시글 생성일
              .updatedAt(post.getUpdatedAt())
              .userId(post.getUser().getId())
              .build();
        })
        .collect(Collectors.toList());
  }

  /**
   * 모든 게시물 전체 조회
   */
  public List<PostResponseDto> getAllPosts() {
    //게시글 모두 조회
    List<Post> posts = postRepository.findAll();

    // 게시글이 없으면 예외 처리
    if (posts.isEmpty()) {
      throw new RuntimeException("게시글이 존재하지 않습니다.");
    }

    return posts.stream()
        .map(post -> {
          // 유튜브 영상 ID 추출 (썸네일 URL 생성)
          String thumbnailUrl = null;
          if (post.getVideo_URL() != null && !post.getVideo_URL().isEmpty()) {
            String videoId = post.getVideo_URL().substring(post.getVideo_URL().length() - 11);
            thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
          }

          List<Pet> pets = post.getUser().getPetList(); // 사용자로부터 반려동물 목록을 가져옴
          String animalType = (pets.isEmpty()) ? "알 수 없음" : pets.get(0).getBreed(); // 첫 번째 반려동물의 품종

          return PostResponseDto.builder()
              .id(post.getId())
              .boardType(post.getBoard().getBoardType().getDisplayName())
              .id(post.getId())
              .title(post.getTitle())
              .animal_type(animalType)
              .content(post.getContent())
              .video_URL(post.getVideo_URL())
              .thumbnail_URL(thumbnailUrl)
              .username(post.getUser().getNickname())
              .profileImage_URL(post.getUser().getProfileImageUrl())
              .images(post.getCommunityImages())
              .like_count(post.getLikeCount())
              .comment_count(post.getCommentCount())
              .createdAt(post.getCreated_at()) // 게시글 생성일
              .updatedAt(post.getUpdatedAt())
              .userId(post.getUser().getId())
              .build();
        })
        .collect(Collectors.toList());
  }


  /**
   * 게시글 수정 API
   */
  public PostResponseDto updatePost(Long postId, @Valid UpdatePostRequestDto postRequestDto,
      List<MultipartFile> newImages, Long currentUserId) {
    // 기존 게시글 조회
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("해당 게시글이 존재하지 않습니다."));

    User user = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

    // 작성자 검증
    if (!post.getUser().getId().equals(currentUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 유저는 수정 권한이 없습니다. 작성자만 가능합니다.");
    }

    // 유튜브 영상 ID 추출
    String thumbnailUrl = null;
    if (postRequestDto.getVideo_URL() != null && !postRequestDto.getVideo_URL().isEmpty()) {
      String videoId = postRequestDto.getVideo_URL()
          .substring(postRequestDto.getVideo_URL().length() - 11);
      thumbnailUrl = "https://img.youtube.com/vi/" + videoId + "/maxresdefault.jpg";
    }

    // 제목과 내용 수정
    post.setTitle(postRequestDto.getTitle());
    post.setContent(postRequestDto.getContent());
    post.setVideo_URL(postRequestDto.getVideo_URL());
    post.setUpdatedAt(LocalDateTime.now());

    // 이미지 삭제
    List<String> removeList = postRequestDto.getRemoveImageUrls();
    if (removeList != null && !removeList.isEmpty()) {
      List<String> images = post.getCommunityImages();
      for (String removeImageUrl : removeList) {
        if (images.contains(removeImageUrl)) {
          images.remove(removeImageUrl);
          s3Manager.deleteObjectByUrl(removeImageUrl);
        }
      }
    }

    // 새 이미지 추가
    if (newImages != null && !newImages.isEmpty()) {
      List<String> images = post.getCommunityImages();
      for (MultipartFile image : newImages) {
        String uuid = UUID.randomUUID().toString();
        Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
        String uploadedUrl = s3Manager.uploadFile(s3Manager.generateCommunity(savedUuid), image);
        images.add(uploadedUrl);
      }
    }

    // 게시글 저장 - DB에 반영
    Post savedPost = postRepository.save(post);

    List<Pet> pets = savedPost.getUser().getPetList(); // 사용자로부터 반려동물 목록을 가져옴
    String animalType = (pets.isEmpty()) ? "알 수 없음" : pets.get(0).getBreed(); // 첫 번째 반려동물의 품종

    // 수정된 게시글 응답 DTO 반환
    return PostResponseDto.builder()
        .id(savedPost.getId())
        .boardType(savedPost.getBoard().getBoardType().getDisplayName())
        .title(savedPost.getTitle())
        .content(savedPost.getContent())
        .video_URL(savedPost.getVideo_URL())
        .thumbnail_URL(thumbnailUrl)
        .animal_type(animalType)
        .username(savedPost.getUser().getNickname())
        .images(savedPost.getCommunityImages()) // 수정된 이미지 목록
        .like_count(savedPost.getLikeCount())
        .profileImage_URL(savedPost.getUser().getProfileImageUrl())
        .comment_count(savedPost.getCommentCount())
        .createdAt(savedPost.getCreated_at()) // 수정된 날짜 그대로 반환
        .updatedAt(savedPost.getUpdatedAt())
        .userId(savedPost.getUser().getId())
        .build();
  }

  /**
   * 게시글 삭제 API
   */
  public void deletePost(Long postId, Long currentUserId) {
    //게시글 조회
    Post post = postRepository.findById(postId)
        .orElseThrow(() -> new RuntimeException("해당 게시물이 존재하지 않습니다"));

    User user = userRepository.findById(currentUserId)
        .orElseThrow(() -> new RuntimeException("해당 유저가 존재하지 않습니다."));

    // 작성자 검증
    if (!post.getUser().getId().equals(currentUserId)) {
      throw new ResponseStatusException(HttpStatus.FORBIDDEN, "해당 유저는 삭제 권한이 없습니다. 작성자만 가능합니다.");
    }

    // 3) S3 이미지 삭제 (있을 경우)
    if (post.getCommunityImages() != null && !post.getCommunityImages().isEmpty()) {
      for (String url : post.getCommunityImages()) {
        s3Manager.deleteObjectByUrl(url);
      }
    }

    // 4) 연관 데이터 삭제
    // - JPA 매핑에 cascade=ALL, orphanRemoval=true가 있으면 아래 한 줄로 모두 정리됩니다.
    postRepository.delete(post);
  }

  /**
   * 인기 게시글 목록을 가져옵니다. 좋아요 수가 10개 이상인 게시글만 가져오고, 좋아요 수 내림차순으로 정렬합니다.
   */
  public List<PostResponseDto> getPopularPosts() {
    List<Post> popularPosts = postRepository.findByLikeCountGreaterThanEqualOrderByLikeCountDesc(
        10);

    return popularPosts.stream()
        .map(post -> {
          List<Pet> pets = post.getUser().getPetList(); // 사용자로부터 반려동물 목록을 가져옴
          String animalType = (pets.isEmpty()) ? "알 수 없음" : pets.get(0).getBreed(); // 첫 번째 반려동물의 품종

          return PostResponseDto.builder()
              .id(post.getId())
              .boardType(post.getBoard().getBoardType().getDisplayName()) // 게시판 이름
              .username(post.getUser().getNickname()) // 사용자 이름
              .title(post.getTitle())
              .content(post.getContent())
              .animal_type(animalType)
              .video_URL(post.getVideo_URL())
              .thumbnail_URL(post.getVideo_URL()) // 비디오 썸네일 추가 (예시)
              .profileImage_URL(post.getUser().getProfileImageUrl()) // 사용자 프로필 이미지
              .images(post.getCommunityImages()) // 게시글 이미지들
              .like_count(post.getLikeCount())
              .comment_count(post.getCommentCount())
              .createdAt(post.getCreated_at())
              .userId(post.getUser().getId())
              .build();
        })
        .collect(Collectors.toList());
  }
}
