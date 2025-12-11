package DC_square.spring.web.dto.response.user;

import DC_square.spring.domain.entity.User;
import DC_square.spring.domain.entity.region.District;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class UserInqueryResponseDto {

  private Long id;
  private String email;
  private String nickname;
  private String phoneNumber;
  private String doName;
  private String si;
  private String gu;
  private Boolean adAgree;
  private String firstPetBreed; // 첫 번째 반려동물 품종
  private String profileImageUrl;
  private Integer gridX;
  private Integer gridY;


  public static UserInqueryResponseDto fromUser(User user, String firstPetBreed) {
    District district = user.getDistrict();

    return UserInqueryResponseDto.builder()
        .id(user.getId())
        .email(user.getEmail())
        .nickname(user.getNickname())
        .phoneNumber(user.getPhoneNumber())
        .doName(district.getCity().getProvince().getName())
        .si(district.getCity().getName())
        .gu(district.getName())
        .adAgree(user.getAdAgree())
        .firstPetBreed(firstPetBreed)
        .profileImageUrl(user.getProfileImageUrl())
        .gridX(user.getDistrict().getCity().getGrid_X())
        .gridY(user.getDistrict().getCity().getGrid_Y())
        .build();
  }
}
