package com.snackoverflow.toolgether.global.token;

import com.snackoverflow.toolgether.global.constants.AppConstants.REFRESH_TOKEN
import com.snackoverflow.toolgether.global.constants.AppConstants.REMEMBER_ME_TOKEN
import com.snackoverflow.toolgether.global.exception.ErrorCode.*
import com.snackoverflow.toolgether.global.exception.ServiceException
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;

import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.time.Instant
import java.util.*;

@Service
class JwtService(
    @Value("\${jwt.secret}") private val secretKey: String // 환경 변수에서 키를 가져옴
) {

    private val log = LoggerFactory.getLogger(JwtService::class.java)

    // JWT 생성
    fun createToken(claims: Map<String, Any>, expiration: Long): String {
        val key: SecretKey = getSecretKey(secretKey) // SecretKey 생성

        log.info("Expiration: {}", expiration)

        return Jwts.builder()
            .claims(claims) // 클레임 설정
            .issuedAt(Date()) // 발급 시간 설정
            .expiration(Date.from(Instant.now().plusMillis(expiration))) // 만료 시간 설정
            .signWith(key) // 서명 키 설정 (알고리즘 자동 선택)
            .compact() // JWT 생성 및 반환
    }

    // JWT 유효성 검사
    fun isValidToken(token: String): Boolean {
        val key = getSecretKey(secretKey) // SecretKey 생성
        return try {
            Jwts.parser() // 파서 생성
                .verifyWith(key) // 서명 검증
                .build() // 빌드
                .parseSignedClaims(token) // 토큰 검증 및 Claims 파싱
            true // 검증 성공 시 true 반환
        } catch (e: JwtException) {
            false // 검증 실패 시 false 반환
        }
    }

    // JWT 검증 및 Claims 객체 추출
    fun parseAndValidateToken(token: String): Claims? {
        val key: SecretKey = getSecretKey(secretKey) // SecretKey 생성

        return try {
            Jwts.parser() // 파서 생성
                .verifyWith(key) // 서명 검증 키 설정
                .build() // 파서 빌드
                .parseSignedClaims(token) // 토큰 검증 및 Claims 추출
                .payload // Claims 객체 반환
        } catch (e: JwtException) {
            handleAuthException(e) // 예외 처리
            null
        }
    }

    // JWT -> 쿠키에 저장 (세션 쿠키)
    fun setJwtSessionCookie(token: String, response: HttpServletResponse) {
        val cookie = ResponseCookie.from(REFRESH_TOKEN, token)
            .httpOnly(true) // 자바스크립트 접근 차단 (XSS 방지)
            .path("/") // 전체 사이트에서 접근 가능
            .sameSite("None") // 외부 사이트 요청 차단 (CSRF 방지)
            .secure(true) // HTTPS 통신 시에만 전송
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    // JWT -> 쿠키에 저장 (지속 쿠키)
    fun setJwtPersistentCookie(token: String, response: HttpServletResponse) {
        val cookie = ResponseCookie.from(REMEMBER_ME_TOKEN, token)
            .httpOnly(true) // 자바스크립트 접근 차단 (XSS 방지)
            .path("/") // 전체 사이트에서 접근 가능
            .sameSite("None") // 외부 사이트 요청 차단 (CSRF 방지)
            .maxAge(Duration.ofDays(90))
            .secure(true) // HTTPS 통신 시에만 전송
            .build()

        response.addHeader("Set-Cookie", cookie.toString())
    }

    // 쿠키에서 JWT 추출 -> 쿠키 이름을 넣어서 반환 받는 것으로 변경
    fun getTokenByCookieName(request: HttpServletRequest, cookieName: String): Optional<String> {
        val cookies = request.cookies ?: return Optional.empty()

        cookies.forEach { cookie ->
            log.info("Cookie Name: {}, Value: {}", cookie.name, cookie.value)
        }

        return cookies.asSequence()
            .filter { it.name == cookieName }
            .map { it.value }
            .firstOrNull()
            ?.let { Optional.of(it) } ?: Optional.empty()
    }

    private fun getSecretKey(secretKey: String): SecretKey {
        // 16진수 문자열 → 바이트 배열로 변환하여 SecretKey 생성
        return Keys.hmacShaKeyFor(secretKey.chunked(2).map { it.toInt(16).toByte() }.toByteArray())
    }

    private fun handleAuthException(e: JwtException) {
        when (e) {
            is io.jsonwebtoken.ExpiredJwtException -> throw ServiceException(errorCode = TOKEN_EXPIRED)
            else -> throw RuntimeException(e.message, e)
        }
    }
}
