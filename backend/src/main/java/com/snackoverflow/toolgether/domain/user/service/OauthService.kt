package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.request.AdditionalInfoRequest
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.entity.User.Companion.createSocialUser
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.ErrorCode
import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;
import kotlin.apply
import kotlin.takeIf

@Service
@Transactional(readOnly = true)
class OauthService(
    private val userRepository: UserRepository,
    private val webClient: WebClient,
    private val locationService: LocationService,
    private val log: Logger,
    @Value("\${spring.security.oauth2.client.registration.google.client-id}") private val clientId: String,
    @Value("\${spring.security.oauth2.client.registration.google.client-secret}") private val clientSecret: String,
    @Value("\${spring.security.oauth2.client.registration.google.redirect-uri}") private val redirectUri: String
) {

    // 토큰 (액세스 토큰 + 리프레시 토큰) 가져오기
    fun getTokens(authCode: String): Map<String, Any> {
        // 구글 토큰 엔드 포인트 URL
        val tokenUrl = "https://oauth2.googleapis.com/token"

        val formData = createFormData(authCode)

        // WebClient를 사용한 POST 요청
        return webClient.post()
            .uri(tokenUrl)
            .contentType(MediaType.APPLICATION_FORM_URLENCODED)
            .body(BodyInserters.fromFormData(formData))
            .retrieve()
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .block()
            ?.takeIf { it.containsKey("access_token") }
            ?: throw ServiceException(ErrorCode.TOKEN_NOT_FOUND)
    }

    private fun createFormData(authCode: String): MultiValueMap<String, String> {
        return LinkedMultiValueMap<String, String>().apply {
            add("code", authCode)
            add("client_id", clientId)
            add("client_secret", clientSecret)
            add("redirect_uri", redirectUri)
            add("grant_type", "authorization_code")
        }
    }

    // 액세스 토큰을 이용해서 유저 정보 가져오기 (Calendar API 추후 추가할 수도 있음)
    fun getUserInfo(accessToken: String): Map<String, Any> {
        val userInfoUrl = "https://www.googleapis.com/oauth2/v3/userinfo"

        return webClient.get()
            .uri(userInfoUrl)
            .header("Authorization", "Bearer " + accessToken)
            .retrieve()
            .onStatus({ it.is4xxClientError }) {
                Mono.error(ServiceException(ErrorCode.TOKEN_EXPIRED))
            }
            .bodyToMono(object : ParameterizedTypeReference<Map<String, Any>>() {})
            .block()
            ?.takeIf { it.containsKey("sub") }
            ?: throw UserNotFoundException()
    }

    @Transactional
    // 소셜 로그인 회원 가입
    fun createSocialUser(userInfo: Map<String, Any>): User {
        val providerId = userInfo["sub"] as String
        val email = userInfo["email"] as String
        val nickname = userInfo["name"] as String
        val phoneNumber = "임시 번호"
        val baseAddress = "임시 주소"
        val provider = "Google"

        return userRepository.save(createSocialUser(providerId, provider, phoneNumber, email, nickname, baseAddress))
    }

    // 소셜 로그인 회원 추가 정보 업데이트
    @Transactional
    fun updateAdditionalInfo(email: String, request: AdditionalInfoRequest) {
        val user = userRepository.findByEmail(email) ?: throw UserNotFoundException()
        val baseAddress = getBaseAddress(request)

        user.apply {
            updatePhoneNumber(request.phoneNumber)
            updateBaseAddress(baseAddress)
        }
    }

    // 외부 api 호출 로직 분리
    fun getBaseAddress(request: AdditionalInfoRequest): String {
        return locationService.convertCoordinateToAddress(request.latitude, request.longitude)
        log.info("클라이언트 위치 정보:{}, {}", request.latitude, request.longitude);
    }
}

