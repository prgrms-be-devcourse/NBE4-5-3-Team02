package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.request.AdditionalInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.custom.user.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class OauthService {

    private final WebClient webClient;
    private final UserRepository userRepository;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    // 토큰 (액세스 토큰 + 리프레시 토큰) 가져오기
    public Map<String, String> getTokens(String authCode) {
        // 구글 토큰 엔드 포인트 URL
        String tokenUrl = "https://oauth2.googleapis.com/token";

        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authCode);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");

        // WebClient를 사용한 POST 요청
        Map<String, String> response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, String>>() {
                })
                .block();// 동기 처리 -> 비동기 처리도 가능한지 알아볼 것
        log.info("Google Token Response: {}", response);
        if (response != null && response.containsKey("access_token"))  {
            return response;
        } else {
            throw new RuntimeException("구글로부터 토큰을 받아오지 못했습니다.");
        }
    }

    // 액세스 토큰을 이용해서 유저 정보 가져오기 (Calendar API 추후 추가할 수도 있음)
    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        // WebClient를 사용하여 GET 요청
        Map<String, Object> response = webClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "Bearer " + accessToken)  // Authorization 헤더에 액세스 토큰 추가
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();
        if (response != null) {
            return response;
        } else {
            throw new RuntimeException("사용자 정보를 가져오지 못했습니다.");
        }
    }

    public boolean existsUser(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public User createSocialUser (Map<String, Object> userInfo) {
        User user = User.builder()
                .providerId((String) userInfo.get("sub"))
                .email((String) userInfo.get("email"))
                .nickname((String) userInfo.get("name"))
                .address(Address.builder()
                        .mainAddress("임시 주소")
                        .detailAddress("임시 주소")
                        .zipcode("12345")
                        .build())
                .provider("Google")
                .additionalInfoRequired(true) // 추가 정보가 필요하다는 필드
                .build();
        return userRepository.save(user);
    }

    @Transactional
    public void updateAdditionalInfo(String email, AdditionalInfoRequest request) {
        User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
        user.updatePhoneNumber(request.phoneNumber());
        user.updateAddress(request.postalCode(), request.baseAddress(), request.detailAddress());
        user.updateLocation(request.latitude(), request.longitude());
        user.updateAdditionalInfoRequired(false); // 추가 정보 입력 후에는 상태 변경
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));
    }

}
