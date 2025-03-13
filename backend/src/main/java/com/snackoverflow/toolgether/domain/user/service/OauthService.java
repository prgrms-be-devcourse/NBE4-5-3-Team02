package com.snackoverflow.toolgether.domain.user.service;

import com.google.api.client.googleapis.auth.oauth2.GoogleTokenResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.client.googleapis.auth.oauth2.GoogleRefreshTokenRequest;
import com.snackoverflow.toolgether.domain.user.dto.KakaoGeoResponse;
import com.snackoverflow.toolgether.domain.user.dto.request.AdditionalInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.custom.location.AddressConversionException;
import com.snackoverflow.toolgether.global.exception.custom.location.DistanceCalculationException;
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
import reactor.core.publisher.Mono;

import java.io.IOException;
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

    @Value("${kakao.rest.api.key}")
    private String kakaoApiKey;

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
                .block();// 동기 처리
        log.info("Google Token Response: {}", response);
        if (response != null && response.containsKey("access_token")) {
            return response;
        } else {
            throw new RuntimeException("구글로부터 토큰을 받아오지 못했습니다.");
        }
    }

    // 액세스 토큰을 이용해서 유저 정보 가져오기 (Calendar API 추후 추가할 수도 있음)
    public Map<String, Object> getUserInfo(String accessToken) {
        String userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo";
        // WebClient 를 사용하여 GET 요청
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
    public User createSocialUser(Map<String, Object> userInfo) {
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

    // 주소 -> 좌표 변환 메서드
    public Mono<KakaoGeoResponse.Document> convertAddressToCoordinate(String baseAddress) {
        String mapUrl = "https://dapi.kakao.com/v2/local/search/address.json?query=" + baseAddress;

        return webClient.get()
                .uri(mapUrl)
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .bodyToMono(KakaoGeoResponse.class)
                .flatMap(response -> {
                    if (response != null && response.getDocuments() != null && !response.getDocuments().isEmpty()) {
                        KakaoGeoResponse.Document document = response.getDocuments().getFirst(); // 첫 번째 결과만 사용
                        log.info("Kakao API의 좌표 -> 주소: {}", document.getAddressName());
                        return Mono.just(document);
                    }
                    return Mono.error(new AddressConversionException("주소를 좌표로 반환할 수 없습니다."));
                });
    }

    // 두 좌표 간의 거리 계산 공식 (Haversine)
    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final double R = 6371; // 지구 반지름 (km 단위)

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // 단위: km
    }

    // 소셜 로그인 회원 추가 정보 업데이트
    @Transactional
    public Mono<Boolean> updateAdditionalInfo(String email, AdditionalInfoRequest request) {
        return Mono.justOrEmpty(userRepository.findByEmail(email))
                .switchIfEmpty(Mono.error(new UserNotFoundException("사용자를 찾을 수 없습니다."))) // 사용자 없음 처리
                .flatMap(user -> {
                    Double clientLat = request.latitude();
                    Double clientLon = request.longitude();
                    log.debug("클라이언트 위치 정보:{}, {}", clientLat, clientLon);

                    return convertAddressToCoordinate(request.baseAddress())
                            .flatMap(converted -> {
                                try {
                                    double addressLat = Double.parseDouble(converted.getLatitude());
                                    double addressLon = Double.parseDouble(converted.getLongitude());
                                    log.debug("주소 -> 좌표 변환 정보: {}, {}", addressLat, addressLon);

                                    double distance = calculateDistance(clientLat, clientLon, addressLat, addressLon);
                                    boolean isWithinRange = distance <= 5; // 5km 이내 여부
                                    log.info("계산된 거리: {} km, 위치 허용 범위 통과 여부: {}", distance, isWithinRange);

                                    if (!isWithinRange) { // 허용 오차범위 초과
                                        return Mono.just(false);
                                    }

                                    // 사용자 정보 업데이트
                                    user.updatePhoneNumber(request.phoneNumber());
                                    user.updateAddress(request.postalCode(), request.baseAddress(), request.detailAddress());
                                    user.updateLocation(addressLat, addressLon);
                                    user.updateAdditionalInfoRequired(false);
                                    userRepository.save(user);

                                    return Mono.just(true);
                                } catch (NumberFormatException e) {
                                    throw new DistanceCalculationException("좌표 변환 중 오류 발생: " + e.getMessage());
                                }
                            })
                            .onErrorResume(e -> { // 모든 오류를 처리
                                if (e instanceof AddressConversionException) {
                                    log.error("주소 변환 중 오류 발생", e);
                                } else if (e instanceof DistanceCalculationException) {
                                    log.error("거리 계산 중 오류 발생", e);
                                } else {
                                    log.error("추가 정보 업데이트 중 알 수 없는 오류 발생", e);
                                }
                                return Mono.just(false); // 실패 시 false 반환
                            });
                });
    }

    // 리프레시 토큰으로 액세스 토큰 재발급
    public String refreshAccessToken(String refreshToken) throws IOException {
        log.info("토큰 재발행 로직 시작");
        GoogleTokenResponse tokenResponse = new GoogleRefreshTokenRequest(
                new NetHttpTransport(),
                GsonFactory.getDefaultInstance(),
                refreshToken,
                clientId,
                clientSecret
        ).execute();

        // 새로 발급된 액세스 토큰
        log.info("새로 발급된 액세스 토큰 = {}", tokenResponse.getAccessToken());
        return tokenResponse.getAccessToken();
    }
}
