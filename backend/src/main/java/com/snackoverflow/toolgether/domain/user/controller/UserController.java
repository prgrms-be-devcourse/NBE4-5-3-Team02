package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.dto.request.LoginRequest;
import com.snackoverflow.toolgether.domain.user.dto.request.SignupRequest;
import com.snackoverflow.toolgether.domain.user.dto.request.VerificationRequest;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.domain.user.service.VerificationService;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import static com.snackoverflow.toolgether.domain.user.service.UserService.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final VerificationService verificationService;
    private final JwtUtil jwtUtil;

    // 이메일 인증
    @PostMapping("/send-verification-code")
    public RsData<String> sendVerificationCode(@Validated @RequestBody EmailRequest request,
                                                       HttpSession session) {
        verificationService.sendEmailWithCode(request.email, session);

        return new RsData<>(
                "200-1",
                "인증 코드가 발송되었습니다.",
                null
        );
    }

    // 이메일 인증 확인
    @PostMapping("/verified-email")
    public RsData<String> verifiedEmail(@RequestBody @Validated VerificationRequest request) {
        verificationService.verifyEmail(request.getEmail(), request.getCode());
        return new RsData<>(
                "201-1",
                "이메일 인증에 성공하였습니다.",
                request.getEmail()
        );
    }

    // 회원 가입
    @PostMapping("/signup")
    public RsData<User> signup(@RequestBody @Validated SignupRequest request) {
        // 이메일, 아이디, 닉네임 중복 확인
        userService.checkDuplicates(request);

        // 이상 없을 시에 회원 가입 진행
        userService.registerVerifiedUser(request);

        return new RsData<>(
                "201-2",
                "회원 가입이 완료되었습니다.",
                userService.getUserForUsername(request.getUsername())
        );
    }

    // 일반 사용자 로그인
    @PostMapping("/login")
    public RsData<String> loginUser(@RequestBody @Validated LoginRequest request,
                                            HttpServletResponse response) {
        LoginResult result = userService.loginUser(request.getUsername(), request.getPassword());
        User user = userService.getUserForUsername(request.getUsername());

        // 쿠키 컨트롤러에서 설정, http 종속성 제거
        jwtUtil.setJwtInCookie(result.token(), response);
        log.info("쿠키에 설정한 토큰 값: " + result.token());

        return new RsData<>(
                "200-2",
                user.getNickname() + " 님 환영합니다!",
                null
        );
    }

    public record EmailRequest(
            @NotBlank(message = "이메일을 입력해 주세요")
            @Email(message = "유효한 이메일 형식이 아닙니다")
            String email) {
    }
}
