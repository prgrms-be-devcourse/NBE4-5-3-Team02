package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.dto.request.AdditionalInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.OauthService;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.Login;
import com.snackoverflow.toolgether.global.util.JwtUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000")
public class UserOauthController {

    private final OauthService oauthService;
    private final JwtUtil jwtUtil;

    @PostMapping("/login/oauth2/code/google")
    public RsData<?> handleAuthCode(@RequestBody Map<String, String> request,
                                    HttpServletResponse response) {
        try {
            String authCode = request.get("code");
            log.info("authCode = {}", authCode);
            if (authCode == null || authCode.isEmpty()) {
                throw new RuntimeException("authCode 값이 없습니다.");
            }

            // 토큰 가져오기
            Map<String, String> tokens = oauthService.getTokens(authCode);

            // 리프레시 토큰을 쿠키에 저장
            setRefreshTokenCookie(response, tokens);

            // 액세스 토큰에서 유저 정보 추출
            Map<String, Object> userInfo = oauthService.getUserInfo(tokens.get("access_token"));
            String email = (String) userInfo.get("email");
            log.info("userInfo = {}", userInfo);

            /**
             * 소셜 로그인 사용자 존재 여부 체크
             * 존재하면 기존 로그인 진행
             * 존재하지 않는 회원이라면 추가 정보 기입으로 이동
             */
            if (oauthService.existsUser(email)) {
                setJwtToken(response, email);
                return new RsData<>(
                        "200-1",
                        "기존 사용자 로그인 성공",
                        null
                );
            }

            User socialUser = oauthService.createSocialUser(userInfo);
            setJwtToken(response, email);
            return new RsData<>(
                    "201-1",
                    "신규 회원 가입 완료 - 추가 정보 입력 필요",
                    Map.of("additionalInfoRequired", socialUser.isAdditionalInfoRequired())
            );
        } catch (Exception e) {
            throw new RuntimeException("액세스 토큰 추출 중 오류 발생!");
        }
    }

    @PatchMapping("/oauth/users/additional-info")
    public RsData<?> updateAdditionalInfo(@Validated @RequestBody AdditionalInfoRequest request,
                                          @Login CustomUserDetails userDetails) {
        oauthService.updateAdditionalInfo(userDetails.getEmail(), request);
        return new RsData<>(
                "201-2",
                "추가 정보가 등록되었습니다.",
                null
        );
    }

    private void setJwtToken(HttpServletResponse response, String email) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("email", email);
        String token = jwtUtil.createToken(claims);
        jwtUtil.setJwtInCookie(token, response);
    }

    private void setRefreshTokenCookie(HttpServletResponse response, Map<String, String> tokens) {
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", tokens.get("refresh_token"))
                .httpOnly(true)
                .secure(true)
                .maxAge(Duration.ofDays(30).toSeconds())
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());
        log.info("refreshToken - cookie 저장: " + refreshCookie.toString());
    }
}
