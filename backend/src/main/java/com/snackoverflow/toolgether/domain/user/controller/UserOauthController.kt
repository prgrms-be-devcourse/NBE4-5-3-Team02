package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.dto.request.AdditionalInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.OauthService;
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.Login;
import com.snackoverflow.toolgether.global.token.JwtService;
import com.snackoverflow.toolgether.global.token.TokenService;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserOauthController {

    @Value("${custom.dev.frontUrl}")
    private String frontUrl;

    @Value("${jwt.access_expiration}")
    private Long access;

    @Value("${jwt.refresh_expiration}")
    private Long refresh;

    private final OauthService oauthService;
    private final TokenService tokenService;
    private final UserServiceV2 userService;
    private final JwtService jwtService;

    @GetMapping("/login/oauth2/code/google")
    public RsData<?> handleSocialLogin(String authCode, HttpServletResponse response) {
        Map<String, Object> tokens = oauthService.getTokens(authCode);
        String accessToken = (String) tokens.get("access_token");

        // 액세스 토큰으로 사용자 정보 가져오기
        Map<String, Object> userInfo = oauthService.getUserInfo(accessToken);
        String email = (String) userInfo.get("email");
        log.info("userInfo = {}", userInfo);

        // 사용자 존재 여부 체크
        if (userService.existsUser(email)) {
            return handleExistingUserLogin(email, response);
        } else {
            return handleNewUserRegistration(userInfo, response);
        }

    }

    // 휴대폰 검증 로직 -> VerifyController 사용하기

    @PatchMapping("/oauth/users/additional-info")
    public RsData<?> updateAdditionalInfo(@Validated AdditionalInfoRequest request,
                                          @Login CustomUserDetails userDetails) {

        oauthService.updateAdditionalInfo(userDetails.getUserEmail(), request);

        // 액세스 토큰 생성 후 바디로 전송
        return new RsData<>(
                "201-2",
                "추가 정보가 등록되었습니다.",
                null);
    }

    // jwt 토큰 추가 (access / refresh)
    private void successLogin(String email, Long userId, HttpServletResponse response) {
        String accessToken = tokenService.createTokenByEmailAndId(email, userId, access);
        // accessToken -> 헤더에 저장
        response.setHeader("Authorization", "Bearer " + accessToken);

        String refreshToken = tokenService.createTokenByEmailAndId(email, userId, refresh);
        // refreshToken 는 세션 쿠키로 저장 (페이지 닫을 시에 만료)
        jwtService.setJwtSessionCookie(refreshToken, response);

        log.info("header 저장 토큰: {}, 쿠키 저장 토큰: {}", accessToken, refreshToken);
    }

    private RsData<?> handleExistingUserLogin(String email, HttpServletResponse response) {
        User user = userService.getUserByEmail(email);

        // 성공적인 로그인 처리
        successLogin(email, user.getId(), response);

        try {
            response.sendRedirect(frontUrl);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage(), e.getCause());
        }

        return new RsData<>(
                "200-1",
                "기존 사용자 로그인 성공",
                Map.of(
                        "user_id", user.getId(),
                        "nickname", user.getNickname()
                )
        );
    }

    private RsData<?> handleNewUserRegistration(Map<String, Object> userInfo, HttpServletResponse response) {
        User socialUser = oauthService.createSocialUser(userInfo);

        // 성공적인 로그인 처리
        successLogin((String) userInfo.get("email"), socialUser.getId(), response);

        try {
            response.sendRedirect(frontUrl + "/additional-info");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return new RsData<>(
                "201-1",
                "신규 회원 가입 완료 - 추가 정보 입력 필요",
                Map.of(
                        "additionalInfoRequired", socialUser.getAdditionalInfoRequired(),
                        "user_id", socialUser.getId(),
                        "nickname", socialUser.getNickname()
                )
        );
    }
}
