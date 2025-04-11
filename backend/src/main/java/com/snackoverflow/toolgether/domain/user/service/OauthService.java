package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.request.AdditionalInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

import static com.snackoverflow.toolgether.global.exception.ErrorCode.TOKEN_EXPIRED;
import static com.snackoverflow.toolgether.global.exception.ErrorCode.TOKEN_NOT_FOUND;

@Slf4j
@Service
@Transactional(readOnly = true)
public class OauthService {

    @Autowired private WebClient webClient;
    private final UserRepository userRepository;
    @Autowired private  LocationService locationService;

    @Value("${spring.security.oauth2.client.registration.google.client-id}")
    private String clientId;

    @Value("${spring.security.oauth2.client.registration.google.client-secret}")
    private String clientSecret;

    @Value("${spring.security.oauth2.client.registration.google.redirect-uri}")
    private String redirectUri;

    public OauthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // 토큰 (액세스 토큰 + 리프레시 토큰) 가져오기
    public Map<String, Object> getTokens(String authCode) {
        // 구글 토큰 엔드 포인트 URL
        String tokenUrl = "https://oauth2.googleapis.com/token";

        LinkedMultiValueMap<String, String> formData = createFormData(authCode);

        // WebClient를 사용한 POST 요청
        Map<String, Object> response = webClient.post()
                .uri(tokenUrl)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(BodyInserters.fromFormData(formData))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {
                })
                .block();// 동기 처리
        log.info("Google Token Response: {}", response);
        if (response != null && response.containsKey("access_token")) {
            return response;
        } else {
            throw new ServiceException(TOKEN_NOT_FOUND);
        }
    }

    // 액세스 토큰을 이용해서 유저 정보 가져오기 (Calendar API 추후 추가할 수도 있음)
    public Map<String, Object> getUserInfo(String accessToken) {
        final String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";

        return webClient.get()
                .uri(userInfoUrl)
                .header("Authorization", "Bearer " + accessToken)
                .retrieve()
                .onStatus(status -> status.is4xxClientError(),
                        response -> Mono.error(new ServiceException(TOKEN_EXPIRED, null)))
                .bodyToMono(new ParameterizedTypeReference<Map<String, Object>>() {})
                .blockOptional()
                .filter(res -> res.containsKey("sub"))
                .orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    // 소셜 로그인 회원 가입
    public User createSocialUser(Map<String, Object> userInfo) {
        // 필요한 값들을 userInfo에서 추출
        String providerId = (String) userInfo.get("sub");
        String email = (String) userInfo.get("email");
        String nickname = (String) userInfo.get("name");
        String phoneNumber = "임시 번호"; // 기본값 설정
        String baseAddress = "임시 주소"; // 기본값 설정
        String provider = "Google"; // 소셜 로그인 제공자

        // createSocialUser 메서드를 사용해서 User 객체 생성
        User user = User.createSocialUser(providerId, provider, phoneNumber, email, nickname, baseAddress);

        // 저장 후 반환
        return userRepository.save(user);
    }


    // 소셜 로그인 회원 추가 정보 업데이트
    @Transactional
    public void updateAdditionalInfo(String email, AdditionalInfoRequest request) {
        User user = userRepository.findByEmail(email);

        Double clientLat = request.getLatitude();
        Double clientLon = request.getLongitude();
        log.info("클라이언트 위치 정보:{}, {}", clientLat, clientLon);

        String baseAddress = locationService.convertCoordinateToAddress(clientLat, clientLon);
        user.updatePhoneNumber(request.getPhoneNumber());
        user.updateBaseAddress(baseAddress);
        user.updateAdditionalInfoRequired(false);
    }


    @NotNull
    private LinkedMultiValueMap<String, String> createFormData(String authCode) {
        LinkedMultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        formData.add("code", authCode);
        formData.add("client_id", clientId);
        formData.add("client_secret", clientSecret);
        formData.add("redirect_uri", redirectUri);
        formData.add("grant_type", "authorization_code");
        return formData;
    }
}
