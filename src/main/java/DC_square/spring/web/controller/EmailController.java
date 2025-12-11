package DC_square.spring.web.controller;

import DC_square.spring.service.EmailService;
import DC_square.spring.web.dto.request.EmailSendRequestDto;
import DC_square.spring.web.dto.request.EmailVerifyRequestDto;
import DC_square.spring.web.dto.response.EmailVerificationResponseDto;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Email", description = "메일인증 관련 API")
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@Slf4j
public class EmailController {

  private final EmailService emailService;

  @Operation(summary = "인증메일 전송 API", description = "입력 된 메일로 인증번호가 전송됩니다.")
  @PostMapping("/send-verification")
  public ResponseEntity<String> sendVerificationEmail(@RequestBody EmailSendRequestDto request) {
    try {
      emailService.sendVerificationEmail(request.getEmail());
      return ResponseEntity.ok("인증 코드가 발송되었습니다.");
    } catch (Exception e) {
      log.error("Failed to send verification email", e);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body("인증 코드 발송에 실패했습니다.");
    }
  }

  @Operation(summary = "인증번호 인증 API", description = "메일과, 인증번호를 인증합니다.")
  @PostMapping("/verify")
  public ResponseEntity<EmailVerificationResponseDto> verifyEmail(
      @RequestBody EmailVerifyRequestDto request) {
    boolean isVerified = emailService.verifyEmail(request.getEmail(),
        request.getVerificationCode());

    EmailVerificationResponseDto response = new EmailVerificationResponseDto();
    response.setVerified(isVerified);
    response.setMessage(isVerified ? "이메일 인증이 완료되었습니다." : "인증 코드가 일치하지 않습니다.");

    return ResponseEntity.ok(response);
  }
}