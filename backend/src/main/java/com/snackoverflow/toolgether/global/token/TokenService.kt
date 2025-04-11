package com.snackoverflow.toolgether.global.token;

import com.snackoverflow.toolgether.global.constants.AppConstants.REFRESH_TOKEN
import com.snackoverflow.toolgether.global.constants.AppConstants.REMEMBER_ME_TOKEN
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
class TokenService(
    @Value("\${jwt.refresh_expiration}") private val refresh: Long,
    @Value("\${jwt.rememberMe_expiration}") private val rememberMe: Long,
    private val jwtService: JwtService // 생성자 주입
) {
    companion object {
        @JvmField // Java에서 static 필드로 접근 가능하도록
        val blacklist = ConcurrentHashMap<String, Long>()
        private val log = LoggerFactory.getLogger(TokenService::class.java)
    }

    // id, 이메일 정보를 담은 토큰 생성 (유효 기간으로 토큰을 나눔)
    fun createTokenByEmailAndId(email: String, id: Long, expiration: Long): String {
        log.info("createTokenByEmailAndId 호출 토큰 생성: {}", expiration)
        val claims = hashMapOf<String, Any>(
            "email" to email,
            "userId" to id
        )
        return jwtService.createToken(claims, expiration)
    }

    // 블랙리스트에서 토큰 확인
    fun isTokenBlacklisted(token: String): Boolean {
        // ConcurrentHashMap에서 값 가져오기, 토큰이 없는 경우에는 false 반환
        val expirationTime = blacklist[token] ?: return false // ConcurrentHashMap에서 값 가져오기

        // 블랙리스트에 토큰이 없는 경우
        // if (expirationTime == null) return false; -> 위의 변수 선언에서 하나로 합침

        // 만료 시간이 지난 경우 제거
        return when {
            System.currentTimeMillis() > expirationTime -> blacklist.remove(token).let { false }
            else -> true
        }
    }

    // 로그아웃 시 쿠키에 있는 토큰 삭제 및 블랙 리스트에 저장
    fun deleteTokens(response: HttpServletResponse, tokenName: String, token: String) {
        log.info("토큰 삭제 로직 시작")

        val blacklist = when (tokenName) {
            REMEMBER_ME_TOKEN -> rememberMe
            REFRESH_TOKEN -> refresh
            else -> return // 처리하지 않는 토큰 이름일 경우 함수 종료하도록
        }

        addToBlacklist(token, blacklist)
        deleteTokenInCookie(response, tokenName)
    }

    fun deleteTokenInCookie(response: HttpServletResponse, tokenName: String) {
        val newToken = Cookie(tokenName, null).apply {
            maxAge = 0 // 쿠키 즉시 삭제
            path = "/" // 전체 경로에 적용
        }
        response.addCookie(newToken) // 클라이언트에 새 쿠키 전송 (기존 쿠키 덮어쓰기)
        log.info("토큰 쿠키가 삭제되었습니다. 쿠키 이름: {}, Value: {}", newToken.name, newToken.value)
    }

    fun addToBlacklist(token: String, expiration: Long) {
        val expirationTime = System.currentTimeMillis() + expiration
        blacklist[token] = expirationTime
        log.info("블랙리스트에 토큰 추가: {}, 만료 시간: {}", token, expirationTime)
    }
}
