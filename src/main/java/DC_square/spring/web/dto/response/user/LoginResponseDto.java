package DC_square.spring.web.dto.response.user;

import DC_square.spring.domain.entity.User;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponseDto {

  private String token;  // JWT access 토큰
  private String refreshToken; // JWT refersh 토큰
  private String email;  // 사용자 이메일
  private String nickname;  // 사용자 닉네임
  private Long userId; //유저 아이디
  private String city;
  private Long cityId; // 시티 아이디(종로구)
  private Long districtId;//(연지동)아이디

  public static LoginResponseDto from(User user, String token, String refreshToken) {
    return LoginResponseDto.builder()
        .token(token)
        .refreshToken(refreshToken)
        .email(user.getEmail())
        .nickname(user.getNickname())
        .userId(user.getId())
        .cityId(user.getDistrict().getCity().getId())
        .districtId(user.getDistrict().getId())
        .build();
  }
}