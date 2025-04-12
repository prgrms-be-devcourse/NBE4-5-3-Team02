package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.v2.KakaoGeoResponseV2;
import com.snackoverflow.toolgether.domain.user.dto.v2.KakaoGeoResponseV2.RoadAddress;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.time.Duration;

import static com.snackoverflow.toolgether.global.exception.ErrorCode.ADDRESS_CONVERSION_ERROR;
import static reactor.util.retry.Retry.fixedDelay;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LocationService {

    @Value("${kakao.rest.api.key}")
    private String kakaoApiKey;

    private final WebClient webClient;
    private final UserRepository userRepository;
    private static final String MAP_URI = "https://dapi.kakao.com/v2/local/geo/coord2address.json?";

    // 위치 변경 로직
    public String updateAddress(Long userId, double latitude, double longitude) {
        String address = convertCoordinateToAddress(latitude, longitude); // 트랜잭션 밖에서 실행
        saveUpdatedAddress(userId, address); // 트랜잭션 내에서 실행
        return address;
    }

    @Transactional
    public void saveUpdatedAddress(Long userId, String address) {
        User user = getUser(userId);
        user.updateBaseAddress(address);
    }

    // 외부 API 호출 → 데이터 검증 → DB 저장이라는 흐름을 보장하기 위해 동기 방식 처리
    public String convertCoordinateToAddress(double latitude, double longitude) {
        String mapUrl = MAP_URI + "x=" + longitude + "&y=" + latitude;
        log.info("MAP API 요청: {}", mapUrl);

        KakaoGeoResponseV2 response = getAddress(mapUrl); // 동기적으로 결과를 기다림
        log.info("변환된 주소: {}", response);

        // 응답 검증
        if (response == null || response.getDocuments() == null || response.getDocuments().isEmpty()) {
            throw new ServiceException(ADDRESS_CONVERSION_ERROR);
        }

        RoadAddress roadAddress = response.getDocuments().getFirst().getRoadAddress();
        // 예시: 서울 성동구 까지만 저장이 되도록 함 (개인정보 보호 용도)
        return roadAddress.getRegion1() + " " + roadAddress.getRegion2();
    }

    private KakaoGeoResponseV2 getAddress(String mapUrl) {
        return webClient.get()
                .uri(mapUrl)
                .header("Authorization", "KakaoAK " + kakaoApiKey)
                .retrieve()
                .onStatus(
                        status -> !status.is2xxSuccessful(),
                        clientResponse -> Mono.error(new ServiceException(ADDRESS_CONVERSION_ERROR, null))
                )
                .bodyToMono(KakaoGeoResponseV2.class)
                .timeout(Duration.ofSeconds(10)) // 타임아웃 설정
                .retryWhen(fixedDelay(3, Duration.ofSeconds(2))) // 최대 3번 재시도, 2초 간격
                .block();
    }

    private User getUser(Long id) {
        return userRepository.findById(id).orElseThrow(UserNotFoundException::new);
    }
}
