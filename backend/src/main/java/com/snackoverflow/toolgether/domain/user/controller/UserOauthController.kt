package com.snackoverflow.toolgether.domain.user.controller

import com.google.common.collect.ImmutableMap
import com.snackoverflow.toolgether.domain.user.dto.request.AdditionalInfoRequest
import com.snackoverflow.toolgether.domain.user.service.OauthService
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2
import com.snackoverflow.toolgether.global.dto.RsData
import com.snackoverflow.toolgether.global.filter.CustomUserDetails
import com.snackoverflow.toolgether.global.filter.Login
import com.snackoverflow.toolgether.global.token.JwtService
import com.snackoverflow.toolgether.global.token.TokenService
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.RestController
import java.io.IOException

@RestController
class UserOauthController(
    @Value("\${custom.site.frontUrl}") private val frontUrl: String,
    @Value("\${jwt.access_expiration}") private val access: Long,
    @Value("\${jwt.refresh_expiration}") private val refresh: Long,
    private val oauthService: OauthService,
    private val tokenService: TokenService,
    private val userService: UserServiceV2,
    private val jwtService: JwtService,
    private val log: Logger
) {

    @GetMapping("/login/oauth2/code/google")
    fun handleSocialLogin(
        authCode: String,
        response: HttpServletResponse
    ): RsData<Any> {

        // 토큰 가져오기
        val accessToken = oauthService.getTokens(authCode)["access_token"] as String

        // 액세스 토큰으로 사용자 정보 가져오기
        val userInfo = oauthService.getUserInfo(accessToken) as Map<String, Any>
        val email = userInfo["email"] as String
        log.info("userInfo = {}", userInfo)

        // 사용자 존재 여부 체크
        return if (userService.existsUser(email)) {
            handleExistingUserLogin(email, response) // 기존에 존재하는 사용자는 로그인 처리
        } else {
            handleNewUserRegistration(userInfo, response) // 존재하지 않은 사용자는 추가 정보 입력
        }
    }

    // 휴대폰 검증 로직 (VerifyController) 이후에 -> 추가 정보 업데이트
    @PatchMapping("/oauth/users/additional-info")
    fun updateAdditionalInfo(
        @Validated request: AdditionalInfoRequest,
        @Login userDetails: CustomUserDetails
    ): RsData<Any> {

        oauthService.updateAdditionalInfo(userDetails.userEmail, request)

        return RsData("201-2", "추가 정보가 등록되었습니다.")
    }

    // JWT 토큰 추가 (access / refresh)
    private fun successLogin(
        email: String,
        userId: Long,
        response: HttpServletResponse
    ) {
        val (accessToken, refreshToken) = listOf(
            tokenService.createTokenByEmailAndId(email, userId, access),
            tokenService.createTokenByEmailAndId(email, userId, refresh)
        )

        response.apply {
            setHeader("Authorization", "Bearer $accessToken") // accessToken -> 헤더에 저장
            jwtService.setJwtSessionCookie(refreshToken, this) // refreshToken는 세션 쿠키로 저장 (페이지 닫을 시에 만료)
        }

        log.info("header 저장 토큰: {}, 쿠키 저장 토큰: {}", accessToken, refreshToken)
    }

    private fun handleExistingUserLogin(
        email: String,
        response: HttpServletResponse
    ): RsData<Any> {

        val user = userService.getUserByEmail(email)

        // 성공적인 로그인 처리
        successLogin(email, user.id!!, response)

        try {
            response.sendRedirect(frontUrl)
        } catch (e: IOException) {
            throw RuntimeException(e.message, e.cause)
        }

        return RsData(
            "200-1", "기존 사용자 로그인 성공",
            mapOf(
                "user_id" to user.id,
                "nickname" to user.nickname
            )
        )
    }

    private fun handleNewUserRegistration(
        userInfo: Map<String, Any>,
        response: HttpServletResponse
    ): RsData<Any> {

        val socialUser = oauthService.createSocialUser(userInfo)

        // 성공적인 로그인 처리
        successLogin(userInfo["email"] as String, socialUser.id!!, response)

        try {
            response.sendRedirect("$frontUrl/additional-info")
        } catch (e: IOException) {
            throw RuntimeException(e)
        }

        return RsData(
            "201-1", "신규 회원 가입 완료 - 추가 정보 입력 필요",
            mapOf(
                "user_id" to socialUser.id,
                "nickname" to socialUser.nickname
            )
        )
    }
}

