package com.snackoverflow.toolgether.domain.user.controller;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.snackoverflow.toolgether.domain.user.dto.request.AdditionalInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.domain.user.service.OauthService;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.exception.custom.user.UserNotFoundException;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.Login;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseCookie;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Map;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserOauthController {

    private final OauthService oauthService;
    private final UserRepository userRepository;

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

            // 구글에서 발행한 리프레시 토큰을 쿠키에 저장
            setRefreshTokenCookie(response, tokens.get("refresh_token"));

            // 액세스 토큰에서 유저 정보 추출
            Map<String, Object> userInfo = oauthService.getUserInfo(tokens.get("access_token"), tokens.get("refresh_token"));
            String email = (String) userInfo.get("email");
            log.info("userInfo = {}", userInfo);

            Multimap<String, String> multiValueMap = ArrayListMultimap.create();
            multiValueMap.put("access_token", tokens.get("access_token"));

            /**
             * 소셜 로그인 사용자 존재 여부 체크
             * 존재하면 기존 로그인 진행 -> 액세스 토큰
             * 존재하지 않는 회원이라면 추가 정보 기입으로 이동
             */
            if (oauthService.existsUser(email)) {
                // 액세스 토큰을 응답 바디에 저장, PK 키 동시 전송
                User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
                return new RsData<>(
                        "200-1",
                        "기존 사용자 로그인 성공",
                        Map.of("access_token", tokens.get("access_token"),
                                "user_id", user.getId(),
                                "nickname", user.getNickname())
                );
            }

            User socialUser = oauthService.createSocialUser(userInfo);
            return new RsData<>(
                    "201-1",
                    "신규 회원 가입 완료 - 추가 정보 입력 필요",
                    Map.of("additionalInfoRequired", socialUser.isAdditionalInfoRequired(),
                            "access_token", tokens.get("access_token"),
                            "user_id", String.valueOf(socialUser.getId()),
                            "nickname", socialUser.getNickname()
                    ));
        } catch (Exception e) {
            throw new RuntimeException("액세스 토큰 추출 중 오류 발생!");
        }
    }

    @PatchMapping("/oauth/users/additional-info")
    public Mono<RsData<Object>> updateAdditionalInfo(
            @Validated @RequestBody AdditionalInfoRequest request,
            @Login CustomUserDetails userDetails) {
        return oauthService.updateAdditionalInfo(userDetails.getEmail(), request)
                .map(isLocationValid -> {
                    if (Boolean.FALSE.equals(isLocationValid)) {
                        log.debug("isLocationValid: {}", isLocationValid);
                        return new RsData<>(
                                "400-1",
                                "위치 정보가 허용 범위를 벗어났습니다.",
                                null);
                    }

                    // 액세스 토큰 생성 후 바디로 전송
                    return new RsData<>(
                            "201-2",
                            "추가 정보가 등록되었습니다.",
                            null);
                });
    }

    @PostMapping("/oauth/token/refresh")
    public RsData<?> refreshAccessToken(@CookieValue("refresh_token") String refreshToken) {
        try {
            String newAccessToken = oauthService.refreshAccessToken(refreshToken);
            return new RsData(
                    "201-1",
                    "토큰 재발행 완료",
                    Map.of("access_token", newAccessToken)
            );
        } catch (Exception e) {
            return new RsData<>(
                    "401-1",
                    "리프레시 토큰이 유효하지 않습니다.",
                    null
            );
        }
    }

    private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .secure(true) // HTTPS에서만 전송
                .path("/") // 특정 경로로 제한
                .maxAge(Duration.ofDays(30).toSeconds()) // 30일
                .sameSite("None")
                .build();

        response.addHeader("Set-Cookie", refreshCookie.toString());
        log.info("refreshToken - cookie 저장: " + refreshCookie.toString());
    }
}
