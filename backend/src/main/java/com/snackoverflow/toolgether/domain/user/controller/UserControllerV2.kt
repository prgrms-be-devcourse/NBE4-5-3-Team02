package com.snackoverflow.toolgether.domain.user.controller

import com.snackoverflow.toolgether.domain.user.dto.v2.LoginRequestV2
import com.snackoverflow.toolgether.domain.user.dto.v2.SignupRequestV2
import com.snackoverflow.toolgether.domain.user.service.MessageService
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2
import com.snackoverflow.toolgether.global.constants.AppConstants.REFRESH_TOKEN
import com.snackoverflow.toolgether.global.constants.AppConstants.REMEMBER_ME_TOKEN
import com.snackoverflow.toolgether.global.dto.RsData
import com.snackoverflow.toolgether.global.token.JwtService
import com.snackoverflow.toolgether.global.token.TokenService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v2")
class UserControllerV2(
    private val userService: UserServiceV2,
    private val messageService: MessageService,
    private val tokenService: TokenService,
    private val jwtService: JwtService,
    private val log: Logger
) {

    /**
     * 바인딩 에러는 글로벌 오류 핸들러에서 일괄 처리: MethodArgumentNotValidException
     */

    @field:Value("\${jwt.access_expiration}")
    private var access: Long = 0

    @field:Value("\${jwt.refresh_expiration}")
    private var refresh: Long = 0

    @field:Value("\${jwt.rememberMe_expiration}")
    private var rememberMe: Long = 0

    // 회원 가입
    @PostMapping("/users/signup")
    fun signup(@RequestBody request: SignupRequestV2): RsData<Any> {
        // 휴대폰 인증 확인
        val isVerified = messageService.isVerified(request.phoneNumber)
        if (!isVerified) {
            return RsData("400", "휴대폰 인증이 필요합니다.")
        }

        // 패스워드 일치 검증 후 가입 진행
        userService.checkPassword(request.password, request.checkPassword)
        val userId = userService.registerUser(request)

        return RsData("201", "회원 가입에 성공하였습니다.", userId)
    }

    @PostMapping("/users/login")
    fun login(@RequestBody request: LoginRequestV2, response: HttpServletResponse): RsData<Any> {
        userService.authenticateUser(request.email, request.password)
        val user = userService.getUserByEmail(request.email)

        // 로그인 성공 시 access / refresh 토큰 발급
        successLogin(request.email, user.id!!, response)
        log.info("로그인 성공, 사용자 정보: {}", user.id)

        if (request.rememberMe == true) {
            addRememberMeToken(request.email, user.id!!, response)
        }

        return RsData("200", "로그인에 성공하였습니다.", mapOf("user_id" to user.id, "nickname" to user.nickname)
        )
    }

    @GetMapping("/logout")
    fun logout(request: HttpServletRequest, response: HttpServletResponse): RsData<Any> {
        jwtService.getTokenByCookieName(request, REFRESH_TOKEN).let { token ->
            tokenService.deleteTokens(response, REFRESH_TOKEN, token.toString())
        }

        jwtService.getTokenByCookieName(request, REMEMBER_ME_TOKEN)?.let { token ->
            tokenService.deleteTokens(response, REMEMBER_ME_TOKEN, token.toString())
        }

        return RsData("200-3", "로그아웃에 성공하였습니다.")
    }

    // remember-me 토큰 추가
    private fun addRememberMeToken(email: String, userId: Long, response: HttpServletResponse) {
        val rememberMeToken = tokenService.createTokenByEmailAndId(email, userId, rememberMe)
        // maxAge 설정 -> 쿠키 저장이 지속됨
        jwtService.setJwtPersistentCookie(rememberMeToken, response)

        log.info("쿠키에 저장된 rememberMe 토큰: {}, 유효기간: {}", rememberMeToken, rememberMe)
    }

    private fun successLogin(email: String, userId: Long, response: HttpServletResponse) {
        val accessToken = tokenService.createTokenByEmailAndId(email, userId, access)
        // accessToken -> 헤더에 저장
        response.setHeader("Authorization", "Bearer $accessToken")

        val refreshToken = tokenService.createTokenByEmailAndId(email, userId, refresh)
        // refreshToken는 세션 쿠키로 저장 (페이지 닫을 시에 만료)
        jwtService.setJwtSessionCookie(refreshToken, response)

        log.info("header 저장 토큰: {}, 쿠키 저장 토큰: {}", accessToken, refreshToken)
    }
}