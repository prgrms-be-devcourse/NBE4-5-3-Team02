package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.v2.KakaoGeoResponseV2
import com.snackoverflow.toolgether.domain.user.dto.v2.KakaoGeoResponseV2.RoadAddress
import com.snackoverflow.toolgether.domain.user.entity.User
import com.snackoverflow.toolgether.domain.user.repository.UserRepository
import com.snackoverflow.toolgether.global.exception.ErrorCode
import com.snackoverflow.toolgether.global.exception.ServiceException
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import reactor.core.publisher.Mono
import reactor.util.retry.Retry
import java.time.Duration
import kotlin.collections.isNullOrEmpty
import kotlin.jvm.java


@Service
@Transactional(readOnly = true)
class LocationService(
    @Value("\${kakao.rest.api.key}") private val kakaoApiKey: String,
    private val webClient: WebClient,
    private val userRepository: UserRepository,
    private val log: Logger
) {
    companion object {
        private const val MAP_URI = "https://dapi.kakao.com/v2/local/geo/coord2address.json?"
    }


    // 위치 변경 로직
    fun updateAddress(userId: Long, latitude: Double, longitude: Double) {
        val address = convertCoordinateToAddress(latitude, longitude) // 트랜잭션 밖에서 실행 (외부 api 호출)
        saveUpdatedAddress(userId, address) // 트랜잭션 내에서 실행
    }

    @Transactional
    fun saveUpdatedAddress(userId: Long, address: String) {
        val user = getUser(userId)
        user.updateBaseAddress(address)
    }

    // 외부 API 호출 → 데이터 검증 → DB 저장이라는 흐름을 보장하기 위해 동기 방식 처리 (코루틴을 활용)
    fun convertCoordinateToAddress(latitude: Double, longitude: Double): String {
        val mapUrl = "$MAP_URI&x=$longitude&y=$latitude"
        log.info("MAP API 요청: {}", mapUrl)

        val response = getAddress(mapUrl)
        log.info("변환된 주소: {}", response)

        // 응답 검증
        if (response.documents.isNullOrEmpty())
            throw ServiceException (ErrorCode.ADDRESS_CONVERSION_ERROR)


        val roadAddress = response.documents.first().roadAddress as RoadAddress

        // 예시: 서울 성동구 까지만 저장이 되도록 함 (개인정보 보호 용도)
        return "${roadAddress.region1} ${roadAddress.region2}"
    }

    private fun getAddress(mapUrl: String): KakaoGeoResponseV2 {
        return webClient.get()
            .uri(mapUrl)
            .header("Authorization", "KakaoAK $kakaoApiKey")
            .retrieve()
            .onStatus({ status -> !status.is2xxSuccessful }) { response ->
                Mono.error(ServiceException(ErrorCode.ADDRESS_CONVERSION_ERROR))
            }
            .bodyToMono(KakaoGeoResponseV2::class.java)
            .timeout(Duration.ofSeconds(10))  // 타임아웃 10초
            .retryWhen(Retry.fixedDelay(3, Duration.ofSeconds(2)))  // 3회 재시도 (2초 간격)
            .block()!!  // 동기 블로킹 호출
    }

    fun getUser(id: Long): User {
        return userRepository.findByUserId(id) ?: throw UserNotFoundException()
    }
}
