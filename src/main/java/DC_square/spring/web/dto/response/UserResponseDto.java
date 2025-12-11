package DC_square.spring.web.dto.response;

import DC_square.spring.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserResponseDto {

  private Long id;
  private String email;
  private String nickname;
  private String phoneNumber;
  private String doName;  // Province name(서울)
  private String si;  // City name(종로구)
  private String gu;    // District name(연지동)
  private Long cityId;
  private Long districtId;
  private String regionId;
  private String profileImageUrl;
  private String token;
  private String refreshToken;

  // 토큰 (회원가입)
  public static UserResponseDto from(User user, String token, String refreshToken) {
    return UserResponseDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .phoneNumber(user.getPhoneNumber())
        .doName(user.getDistrict().getCity().getProvince().getName())
        .si(user.getDistrict().getCity().getName())
        .gu(user.getDistrict().getName())
        .cityId(user.getDistrict().getCity().getId())
        .districtId(user.getDistrict().getId())
        .profileImageUrl(user.getProfileImageUrl())
        .token(token)
        .refreshToken(refreshToken)
        .build();
  }

  // 일반 사용자 조회
  public static UserResponseDto from(User user) {
    return UserResponseDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .phoneNumber(user.getPhoneNumber())
        .doName(user.getDistrict().getCity().getProvince().getName())
        .si(user.getDistrict().getCity().getName())
        .gu(user.getDistrict().getName())
        .cityId(user.getDistrict().getCity().getId())
        .districtId(user.getDistrict().getId())
//                .profileImageUrl(user.getProfileImageUrl())
        .build();

  }
}