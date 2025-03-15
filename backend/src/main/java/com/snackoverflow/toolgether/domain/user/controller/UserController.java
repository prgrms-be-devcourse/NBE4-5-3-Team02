package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.dto.request.EmailRequest;
import com.snackoverflow.toolgether.domain.user.dto.request.LoginRequest;
import com.snackoverflow.toolgether.domain.user.dto.request.SignupRequest;
import com.snackoverflow.toolgether.domain.user.dto.request.VerificationRequest;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.domain.user.service.VerificationService;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.Login;
import com.snackoverflow.toolgether.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

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
        verificationService.sendEmailWithCode(request.getEmail(), session);

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
    public RsData<?> signup(@RequestBody @Validated SignupRequest request) {
        try {
            // 중복 확인 및 패스워드가 일치하는지 확인
            userService.checkDuplicates(request);
            userService.checkPassword(request);

            // 회원 가입 처리
            boolean isSuccess = userService.registerVerifiedUser(request);

            if (isSuccess) {
                // 회원 정보 조회 및 성공 응답 반환
                User user = userService.getUserForUsername(request.username());
                return new RsData<>("201-2", "회원 가입 완료", user);
            }

            // 위치 검증 실패 응답 반환
            return new RsData<>("400-1", "위치 검증 실패", null);

        } catch (Exception e) {
            // 기타 서버 오류 처리
            return new RsData<>("500-1", "서버 오류", null);
        }
    }

    // 일반 사용자 로그인
    @PostMapping("/login")
    public RsData<?> loginUser(@RequestBody @Validated LoginRequest request,
                                    HttpServletResponse response) {
        LoginResult result = userService.loginUser(request.getUsername(), request.getPassword());
        User user = userService.getUserForUsername(request.getUsername());

        // 쿠키 컨트롤러에서 설정, http 종속성 제거
        jwtUtil.setJwtInCookie(result.token(), response);
        log.info("쿠키에 설정한 토큰 값: " + result.token());

        return new RsData<>(
                "200-2",
                user.getNickname() + " 님 환영합니다!",
                Map.of(
                        "nickname", user.getNickname(),
                        "token", result.token()
                )
        );
    }

    @GetMapping("/profile")
    public RsData<String> getProfile(
            @Login CustomUserDetails customUserDetails
    ) {
        Long userId = customUserDetails.getUserId();
        String profile = userService.getMyProfile(userId);
        return new RsData<>(
                "200-1",
                "프로필 조회 성공",
                profile
        );
    }

    @GetMapping("/{id}")
    public RsData<User> getUser(@PathVariable Long id) {
        User user = userService.findUserById(id);
        return new RsData<>(
            "200-3",
            "%d번 유저 검색".formatted(id),
            user
        );
    }
}
