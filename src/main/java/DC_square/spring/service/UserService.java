package DC_square.spring.service;


import DC_square.spring.config.S3.AmazonS3Manager;
import DC_square.spring.config.S3.Uuid;
import DC_square.spring.config.S3.UuidRepository;
import DC_square.spring.config.jwt.JwtTokenProvider;
import DC_square.spring.constant.ImageConstants;
import DC_square.spring.domain.entity.Dday;
import DC_square.spring.domain.entity.Pet;
import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.community.Post;
import DC_square.spring.domain.entity.region.District;
import DC_square.spring.domain.enums.DdayType;
import DC_square.spring.repository.PetRepository;
import DC_square.spring.repository.RegionRepository;
import DC_square.spring.repository.WalkRepository.WalkRepository;
import DC_square.spring.repository.WalkRepository.WalkReviewLikeRepository;
import DC_square.spring.repository.WalkRepository.WalkReviewRepository;
import DC_square.spring.repository.WalkRepository.WalkWishRepository;
import DC_square.spring.repository.community.CommentRepository;
import DC_square.spring.repository.community.PostLikeRepository;
import DC_square.spring.repository.community.PostRepository;
import DC_square.spring.repository.community.UserRepository;
import DC_square.spring.repository.dday.DdayRepository;
import DC_square.spring.repository.place.PlaceReviewRepository;
import DC_square.spring.repository.place.PlaceWishRepository;
import DC_square.spring.repository.region.CityRepository;
import DC_square.spring.repository.region.DistrictRepository;
import DC_square.spring.repository.region.ProvinceRepository;
import DC_square.spring.web.dto.request.LoginRequestDto;
import DC_square.spring.web.dto.request.user.PetRegistrationDto;
import DC_square.spring.web.dto.request.user.UserRegistrationRequestDto;
import DC_square.spring.web.dto.request.user.UserUpdateRequestDto;
import DC_square.spring.web.dto.response.PetResponseDto;
import DC_square.spring.web.dto.response.UserResponseDto;
import DC_square.spring.web.dto.response.user.LoginResponseDto;
import DC_square.spring.web.dto.response.user.UserInqueryResponseDto;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

  private final JwtTokenProvider jwtTokenProvider;
  private final PasswordEncoder passwordEncoder;
  private final UserRepository userRepository;
  private final ProvinceRepository provinceRepository;
  private final CityRepository cityRepository;
  private final DistrictRepository districtRepository;
  private final RegionRepository regionRepository;
  private final PetRepository petRepository;
  private final DdayRepository ddayRepository;
  private final UuidRepository uuidRepository;
  private final AmazonS3Manager s3Manager;
  private final RegionService regionService;
  private final PostRepository postRepository;
  private final PostLikeRepository postLikeRepository;
  private final CommentRepository commentRepository;
  private final PlaceWishRepository placeWishRepository;
  private final PlaceReviewRepository placeReviewRepository;
  private final WalkWishRepository walkWishRepository;
  private final WalkReviewRepository walkReviewRepository;
  private final WalkRepository walkRepository;
  private final WalkReviewLikeRepository walkReviewLikeRepository;


  @Transactional
  public UserResponseDto createUser(UserRegistrationRequestDto request, MultipartFile profileImage,
      MultipartFile petImage) {
    // 이메일 중복 검사
    if (userRepository.existsByEmail(request.getEmail())) {
      throw new RuntimeException("이미 존재하는 이메일입니다.");
    }

    // 닉네임 중복 검사
    if (userRepository.existsByNickname(request.getNickname())) {
      throw new RuntimeException("이미 존재하는 닉네임입니다.");
    }

//        // 지역 정보 조회 또는 생성
//        Region region = regionRepository.findByDoNameAndSiAndGu(
//                request.getDoName(),
//                request.getSi(),
//                request.getGu()
//        ).orElseGet(() -> { //조회 결과 없을 때 새롭게 생성 !
//            Region newRegion = Region.builder()
//                    .doName(request.getDoName())
//                    .si(request.getSi())
//                    .gu(request.getGu())
//                    .build();
//            return regionRepository.save(newRegion);
//        });
    // 지역 정보 조회 또는 생성
    District district = regionService.findOrCreateDistrict(
        request.getDoName(),
        request.getSi(),
        request.getGu()
    );

    // 프로필 이미지 업로드
    String profileImageUrl = ImageConstants.DEFAULT_PROFILE_IMAGE;  // 기본 이미지 설정
    if (profileImage != null && !profileImage.isEmpty()) {
      String uuid = UUID.randomUUID().toString();
      Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
      profileImageUrl = s3Manager.uploadFile(s3Manager.generateProfile(savedUuid), profileImage);
    }

    // RequestDto -> Entity, DB에 저장
    User user = User.builder()
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword())) // 비밀번호 변환
        .nickname(request.getNickname())
        .phoneNumber(request.getPhoneNumber())
        // .regionId(region.getId().toString())
        .district(district)  // district를 직접 참조
        .adAgree(request.getAdAgree())
        .profileImageUrl(profileImageUrl)
        .build();

    // 저장
    User savedUser = userRepository.save(user);

    // 반려동물 저장
    String petImageUrl = ImageConstants.DEFAULT_PET_IMAGE;  // 기본 이미지 설정
    if (petImage != null && !petImage.isEmpty()) {
      String uuid = UUID.randomUUID().toString();
      Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
      petImageUrl = s3Manager.uploadFile(s3Manager.generatePet(savedUuid), petImage);
    }

    Pet pet = Pet.builder()
        .petName(request.getPet().getPetName())
        .dogCat(request.getPet().getDogCat())
        .breed(request.getPet().getBreed())
        .birth(request.getPet().convertBirthToLocalDate())
        .user(savedUser)
        .petImageUrl(petImageUrl)
        .build();

    petRepository.save(pet);

    // D-day 생성 - 사료 구매
    LocalDate foodDate = LocalDate.parse(request.getFoodDate());
    Dday foodDday = Dday.builder()
        .title("사료 구매")
        .day(foodDate.plusWeeks(request.getFoodDuring()))
        .term(request.getFoodDuring())
        .user(savedUser)
        .type(DdayType.FOOD)
        .build();
    foodDday.setDefaultImageUrl();
    ddayRepository.save(foodDday);

    // D-day 생성 - 패드/모래 구매
    LocalDate padDate = LocalDate.parse(request.getPadDate());
    Dday padDday = Dday.builder()
        .title("패드/모래 구매")
        .day(padDate.plusWeeks(request.getPadDuring()))
        .term(request.getPadDuring())
        .user(savedUser)
        .type(DdayType.PAD)
        .build();
    padDday.setDefaultImageUrl();
    ddayRepository.save(padDday);

    // D-day 생성 - 병원 방문일
    LocalDate hospitalDate = LocalDate.parse(request.getHospitalDate());
    Dday hospitalDday = Dday.builder()
        .title("병원 방문일")
        .day(hospitalDate)
        .user(savedUser)
        .type(DdayType.HOSPITAL)
        .build();
    hospitalDday.setDefaultImageUrl();
    ddayRepository.save(hospitalDday);

    // 토큰 생성
    String token = jwtTokenProvider.createAccessToken(savedUser.getEmail());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());

    // 토큰을 포함한 응답 반환
    return UserResponseDto.from(savedUser, token, refreshToken);
  }

  // 로그인
  public LoginResponseDto login(LoginRequestDto request) {
    //이메일 여부 확인
    User user = userRepository.findByEmail(request.getEmail())
        .orElseThrow(() -> new RuntimeException("존재하지 않는 이메일입니다."));
    // 비밀번호 일치 확인
    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      throw new RuntimeException("비밀번호가 일치하지 않습니다.");
    }
    // 토큰 생성
    String token = jwtTokenProvider.createAccessToken(user.getEmail());
    String refreshToken = jwtTokenProvider.createRefreshToken(user.getEmail());
    return LoginResponseDto.from(user, token, refreshToken);
  }

  // 이메일 중복 확인
  public boolean checkEmailDuplicate(String email) {
    return userRepository.existsByEmail(email);
  }

  // 닉네임 중복  확인
  public boolean checkNicknameDuplicate(String nickname) {
    return userRepository.existsByNickname(nickname);
  }

  public UserResponseDto findUserById(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    return UserResponseDto.from(user);
  }

  // 유저 조회
  @Transactional(readOnly = true)
  public UserInqueryResponseDto getUserInfo(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

//        // 사용자의 regionId로 Region 정보 조회
//        Region region = regionRepository.findById(Long.parseLong(user.getRegionId()))
//                .orElseThrow(() -> new RuntimeException("지역 정보를 찾을 수 없습니다."));

    // district를 통해 지역 정보 조회 (이미 연관관계로 매핑되어 있음)
    District district = user.getDistrict();
    if (district == null) {
      throw new RuntimeException("지역 정보를 찾을 수 없습니다.");
    }

    // 첫 번째 반려동물의 품종 가져오기
    String firstPetBreed = petRepository.findAllByUser(user).stream()
        .findFirst()
        .map(Pet::getBreed)
        .orElse(null);

    return UserInqueryResponseDto.fromUser(user, firstPetBreed);
  }

  // 반려동물 추가
  @Transactional
  public UserResponseDto addPet(Long userId, PetRegistrationDto request, MultipartFile petImage) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    String petImageUrl = null;
    if (petImage != null && !petImage.isEmpty()) {
      String uuid = UUID.randomUUID().toString();
      Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
      petImageUrl = s3Manager.uploadFile(s3Manager.generatePet(savedUuid), petImage);
    }

    // 반려동물 저장

    Pet pet = Pet.builder()
        .petName(request.getPetName())
        .dogCat(request.getDogCat())
        .breed(request.getBreed())
        .birth(request.convertBirthToLocalDate())
        .user(user)
        .petImageUrl(petImageUrl)
        .build();

    petRepository.save(pet);

    // 업데이트된 사용자 정보 반환
    return UserResponseDto.from(user);
  }

  // 반려동물 전체 조회
  @Transactional(readOnly = true)
  public List<PetResponseDto> getPets(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    // 해당 사용자의 모든 반려동물 조회 후 DTO로 변환
    return petRepository.findAllByUser(user).stream()
        .map(PetResponseDto::from)
        .collect(Collectors.toList());
  }

  // 반려동물 상세조회
  @Transactional(readOnly = true)
  public PetResponseDto getPetDetail(Long userId, Long petId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    Pet pet = petRepository.findById(petId)
        .orElseThrow(() -> new RuntimeException("반려동물을 찾을 수 없습니다."));

    // 해당 반려동물이 요청한 사용자의 소유인지 확인
    if (!pet.getUser().getId().equals(userId)) {
      throw new RuntimeException("해당 반려동물의 정보를 조회할 권한이 없습니다.");
    }
    // 반려동물 정보 반환
    return PetResponseDto.from(pet);
  }


  // 반려동물 삭제
  @Transactional
  public void deletePet(Long userId, Long petId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    Pet pet = petRepository.findById(petId)
        .orElseThrow(() -> new RuntimeException("반려동물을 찾을 수 없습니다."));

    // 해당 반려동물이 요청한 사용자의 소유인지 확인
    if (!pet.getUser().getId().equals(userId)) {
      throw new RuntimeException("해당 반려동물을 삭제할 권한이 없습니다.");
    }

    // 반려동물 삭제
    petRepository.delete(pet);
  }

  //반려동물 수정
  @Transactional
  public PetResponseDto modifyPet(Long userId, Long petId, PetRegistrationDto request,
      MultipartFile petImage) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    Pet pet = petRepository.findById(petId)
        .orElseThrow(() -> new RuntimeException("반려동물을 찾을 수 없습니다."));

    // 해당 반려동물이 요청한 사용자의 소유인지 확인
    if (!pet.getUser().getId().equals(userId)) {
      throw new RuntimeException("해당 반려동물을 수정할 권한이 없습니다.");
    }

    String petImageUrl = pet.getPetImageUrl();
    if (petImage != null && !petImage.isEmpty()) {
      String uuid = UUID.randomUUID().toString();
      Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
      petImageUrl = s3Manager.uploadFile(s3Manager.generatePet(savedUuid), petImage);
    }

    // 반려동물 정보 업데이트
    pet.setPetName(request.getPetName());
    pet.setDogCat(request.getDogCat());
    pet.setBreed(request.getBreed());
    pet.setBirth(request.convertBirthToLocalDate());
    pet.setPetImageUrl(petImageUrl);

    // 변경 사항 저장
    Pet updatedPet = petRepository.save(pet);

    // 수정된 반려동물 정보 반환
    return PetResponseDto.from(updatedPet);
  }

  //회원 정보 수정
  @Transactional
  public UserResponseDto updateUser(String email, UserUpdateRequestDto updateDto,
      MultipartFile profileImage) {
    User user = userRepository.findByEmail(email)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

    // 닉네임 중복 체크
    if (updateDto.getNickname() != null && !updateDto.getNickname().equals(user.getNickname())) {
      if (userRepository.existsByNickname(updateDto.getNickname())) {
        throw new RuntimeException("이미 존재하는 닉네임입니다.");
      }
      user.setNickname(updateDto.getNickname());
    }

    // 전화번호 업데이트
    if (updateDto.getPhoneNumber() != null) {
      user.setPhoneNumber(updateDto.getPhoneNumber());
    }

    // 비밀번호 업데이트
    if (updateDto.getPassword() != null) {
      user.setPassword(passwordEncoder.encode(updateDto.getPassword()));
    }

    // 프로필 이미지 업데이트
    if (profileImage != null && !profileImage.isEmpty()) {
      String uuid = UUID.randomUUID().toString();
      Uuid savedUuid = uuidRepository.save(Uuid.builder().uuid(uuid).build());
      String profileImageUrl = s3Manager.uploadFile(s3Manager.generateProfile(savedUuid),
          profileImage);
      user.setProfileImageUrl(profileImageUrl);
    }

    User savedUser = userRepository.save(user);
    //String token = jwtTokenProvider.createToken(savedUser.getEmail());
    return UserResponseDto.from(savedUser);
  }

  @Transactional
  public void deleteUser(Long userId) {
    User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
    // 게시글의 좋아요 삭제
    postLikeRepository.deleteAll(postLikeRepository.findAllByUser(user));

    // 사용자의 게시글 조회
    List<Post> userPosts = postRepository.findByUserId(user.getId());

    // 각 게시글의 댓글 삭제
    for (Post post : userPosts) {
      commentRepository.deleteAll(commentRepository.findByPostId(post.getId()));
    }

    // 게시글 삭제
    postRepository.deleteAll(userPosts);

    // 커뮤니티 관련
//        myBoardRepository.deleteAll(myBoardRepository.findByUserOrderByIdAsc(user));

    // 장소 관련
    placeWishRepository.deleteAll(placeWishRepository.findAllByUserId(user.getId()));
    placeReviewRepository.deleteAll(placeReviewRepository.findAllByUserId(user.getId()));

    // 산책 관련
    walkWishRepository.deleteAll(walkWishRepository.findByUserAndIsWished(user, true));
    walkReviewRepository.deleteAll(walkReviewRepository.findAllByUserId(user.getId()));

    // 연관된 D-day 삭제
    ddayRepository.deleteAll(ddayRepository.findAllByUser(user));

    // 연관된 반려동물 삭제
    petRepository.deleteAll(petRepository.findAllByUser(user));

    // 산책 관련 - Walk 데이터 삭제
    // Walk 리뷰 좋아요 삭제
    walkReviewLikeRepository.deleteAll(walkReviewLikeRepository.findByUserId(user.getId()));
    // Walk 리뷰 삭제
    walkReviewRepository.deleteAll(walkReviewRepository.findAllByUserId(user.getId()));
    // Walk 위시리스트 삭제
    walkWishRepository.deleteAll(walkWishRepository.findByUserAndIsWished(user, true));
    // 사용자가 작성한 Walk 삭제
    walkRepository.deleteAll(walkRepository.findByCreatedById(user.getId()));

    // 사용자 삭제 - 나머지 연관 엔티티 ->  cascade 삭제
    userRepository.delete(user);

  }

  public Long findUserIdByEmail(String email) {
    return userRepository.findByEmail(email)
        .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."))
        .getId();
  }

//    @Transactional
//    public void saveFirebaseToken(Long id, String fcmToken) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new UsernameNotFoundException("User not found with id: " + id));
//        user.setFcmToken(fcmToken);
//        userRepository.save(user);
//    }
//
//    public String findFirebaseTokenById(Long id) {
//        return userRepository.findById(id)
//                .map(User::getFcmToken)
//                .orElseThrow(() -> new UsernameNotFoundException("Firebase token not found for user: " + id));
//    }

}