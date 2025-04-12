package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.dto.request.EmailRequest;
import com.snackoverflow.toolgether.domain.user.dto.request.VerificationData;
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2;
import com.snackoverflow.toolgether.domain.user.service.VerificationService;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.Login;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static com.snackoverflow.toolgether.global.constants.AppConstants.SESSION_KEY;

@Slf4j
@RestController
@RequestMapping("/api/v2/users")
@RequiredArgsConstructor
public class UserInfoController {

    @Value("${custom.dev.frontUrl}")
    private String frontUrl;

    private final UserServiceV2 userService;
    private final VerificationService verificationService;

    // 이메일 찾기 -> 휴대폰 인증 send, verify
    @PostMapping("/find-email")
    public RsData<?> findEmail(@RequestParam String phoneNumber) {
        String email = userService.getUserEmail(phoneNumber);
        return new RsData<>(
                "200",
                "고객님의 이메일: " + email,
                email);
    }

    // 비밀번호 변경 -> 이메일 인증 확인 링크 전송
    @PostMapping("/send-verification")
    public RsData<String> sendVerification(@Validated @RequestBody EmailRequest request,
                                               HttpSession session) {
        verificationService.sendEmailWithCode(request.getEmail(), session);

        return new RsData<>(
                "200-1",
                "인증 메일이 발송되었습니다.",
                null
        );
    }

    // 이메일 인증 확인
    @GetMapping("/verify")
    public RsData<?> verifyEmail(@RequestParam("code") String code,
                                 HttpSession session,
                                 HttpServletResponse response) throws IOException {

        // 세션에서 인증 데이터 가져오기
        VerificationData data = (VerificationData) session.getAttribute(SESSION_KEY);
        // 인증 코드 검증
        if (data != null && data.getCode().equals(code)) {
            // 인증 성공 처리
            log.info("이메일 링크 인증 성공 - 세션 정보: {}, 입력 정보:{}", data.getCode(), code);
            data.setVerified(true);
            session.setAttribute(SESSION_KEY, data);
            response.sendRedirect(frontUrl + "/success");
            return new RsData<>(
                    "201",
                    "이메일 인증에 성공하였습니다.",
                    null
            );
        }
        response.sendRedirect(frontUrl + "/fail");
        return new RsData<>(
                "400",
                "이메일 인증에 실패했습니다.",
                null
        );
    }

    @PostMapping("/change-password")
    public RsData<?> changePassword(@Login CustomUserDetails userDetails,
                                    @RequestBody PasswordRequest password) {

        // 이전 패스워드와 같은 걸론 변경할 수 없음
        userService.checkBeforePassword(userDetails.getUserId(), password.password);

        // 패스워드 변경
        userService.changePassword(userDetails.getUserId(), password.password);

        return new RsData<>(
                "200",
                "비밀번호 변경에 성공했습니다",
                null);
    }

    private record PasswordRequest(String password) {}
}
