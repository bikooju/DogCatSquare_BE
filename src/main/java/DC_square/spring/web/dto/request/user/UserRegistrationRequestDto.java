package DC_square.spring.web.dto.request.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserRegistrationRequestDto {

  // 사용자 정보
  @NotBlank(message = "이메일은 필수입니다.")
  @Email(message = "올바른 이메일 형식이 아닙니다.")
  private String email;

  @NotBlank(message = "비밀번호는 필수입니다.")
  @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다.")
  @Schema(example = "password123")
  private String password;

  @NotBlank(message = "닉네임은 필수입니다.")
  @Size(min = 2, max = 10, message = "닉네임은 2자 이상 10자 이하여야 합니다.")
  private String nickname;

  @NotBlank(message = "전화번호는 필수입니다.")
  @Pattern(regexp = "^\\d{11}$", message = "전화번호는 11자리 숫자여야 합니다.")
  @Schema(example = "01012341234")
  private String phoneNumber;


  @NotBlank(message = "도/특별시/광역시는 필수입니다.")
  private String doName;

  @NotBlank(message = "구는 필수입니다.")
  private String gu;

  @NotBlank(message = "시/동 는 필수입니다.")
  private String si;


  // 반려동물 정보 리스트
  @NotEmpty(message = "최소 한 마리의 반려동물 정보가 필요합니다.")
  @Valid
  private PetRegistrationDto pet; //한 마리만 등록하는걸로 디자인 변경

  @NotBlank(message = "사료 구매일은 필수입니다.")
  @Schema(example = "2025-01-12")
  private String foodDate;    // yyyy-MM-dd

  @NotNull(message = "사료 구매 주기는 필수입니다.")
  private Integer foodDuring;  // 주 단위

  @NotBlank(message = "패드/모래 구매일은 필수입니다.")
  @Schema(example = "2025-01-12")
  private String padDate;     // yyyy-MM-dd

  @NotNull(message = "패드/모래 구매 주기는 필수입니다.")
  private Integer padDuring;   // 주 단위

  @NotBlank(message = "병원 방문일은 필수입니다.")
  @Schema(example = "2025-01-12")
  private String hospitalDate;  // yyyy-MM-dd

  @NotNull(message = "광고여부는 필수입니다.")
  private Boolean adAgree;

}
