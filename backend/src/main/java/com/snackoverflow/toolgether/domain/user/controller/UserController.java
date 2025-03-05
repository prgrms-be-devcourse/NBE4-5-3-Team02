package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.dto.SignupRequest;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.domain.user.service.VerificationService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final VerificationService verificationService;

    // 이메일 인증
    @PostMapping("/send-verification-code")
    public ResponseEntity<String> sendVerificationCode(@Validated @RequestBody EmailRequest request,
                                                       HttpSession session) {
        verificationService.sendEmailWithCode(request.email, session);
        return ResponseEntity.ok().body("인증 코드가 발송되었습니다.");
    }

    // 이메일 인증 확인
    @PostMapping("/verified-email")
    public ResponseEntity<String> verifiedEmail(@RequestBody @Validated VerificationRequest request) {
        verificationService.verifyEmail(request.getEmail(), request.code);
        return ResponseEntity.status(201).body("이메일 인증에 성공하였습니다.");
    }

    // 회원 가입
    @PostMapping("/signup")
    public ResponseEntity<String> signup(@RequestBody @Validated SignupRequest request) {
        // 이메일, 아이디, 닉네임 중복 확인
        userService.checkDuplicates(request);

        // 이상 없을 시에 회원 가입 진행
        userService.registerVerifiedUser(request);
        return ResponseEntity.status(201).body("회원 가입이 완료되었습니다.");
    }

    public record EmailRequest(
            @NotBlank(message = "이메일을 입력해 주세요")
            @Email(message = "유효한 이메일 형식이 아닙니다")
            String email) {
    }

    @Data
    static class VerificationRequest {
        @NotBlank(message = "이메일을 입력해 주세요")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "인증 코드를 입력해주세요")
        @Size(min = 8, max = 8, message = "인증 코드는 8자리여야 합니다")
        @Pattern(regexp = "^[A-Za-z0-9]{8}$", message = "영문 대소문자와 숫자 조합 8자리를 입력해주세요")
        private String code;
    }
}
