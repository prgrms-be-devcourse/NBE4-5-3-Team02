package com.snackoverflow.toolgether.global.util;

import com.snackoverflow.toolgether.global.exception.custom.mail.CustomAuthException;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SecurityException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.time.Duration;
import java.util.*;

@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long expiration;

    // JWT 생성
    public String createToken(Map<String, Object> claims) {
        SecretKey key = getSecretKey();
        Date issuedAt = new Date();

        return Jwts.builder()
                .claims(claims)
                .issuedAt(issuedAt)
                .expiration(new Date(issuedAt.getTime() + expiration))
                .signWith(key) // 알고리즘 자동 인식 (HS256)
                .compact();
    }

    // JWT 유효성 검사
    private boolean isValidToken(String token) {
        try {
            SecretKey key = getSecretKey();
            Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException | MalformedJwtException | IllegalArgumentException |
                UnsupportedJwtException | SecurityException e) {
            handleAuthException(e);
            return false;
        }
    }

    // JWT 페이로드 추출
    public Claims getPayload(String token) {
        try {
            SecretKey key = getSecretKey();
            return Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload(); // Claims 객체 직접 반환
        } catch (JwtException | IllegalArgumentException e) {
            handleAuthException(e);
            return null;
        }
    }

    //JWT -> 쿠키에 저장
    public void setJwtInCookie(String token, HttpServletResponse response) {
        ResponseCookie cookie = ResponseCookie.from("token", token)
                .httpOnly(true) // 자바스크립트 접근 차단 (XSS 방지)
                .path("/") // 전체 사이트에서 접근 가능
                .sameSite("None") // 외부 사이트 요청 차단 (CSRF 방지)
                .maxAge(Duration.ofMinutes(30)) // Access Token 만료 시간
                .secure(true) // HTTPS 통신 시에만 전송
                .build();

        response.addHeader("Set-Cookie", cookie.toString());
        log.info("token = {}", cookie.toString());
    }

    // 쿠키에서 JWT 추출
    public Optional<String> getJwtFromCookies(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();

        if (cookies == null || cookies.length == 0) { // 쿠키가 없을 경우
            return Optional.empty();
        }

        Arrays.stream(cookies)
                .forEach(cookie -> log.info("Cookie Name: {}, Value: {}", cookie.getName(), cookie.getValue()));

        return Arrays.stream(cookies)
                .filter(cookie -> "token".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst();
    }

    private SecretKey getSecretKey() {
        SecretKey key = Keys.hmacShaKeyFor(HexFormat.of().parseHex(secretKey)); // 16진수 문자열 → 바이트 배열
        return key;
    }

    // 예외 처리 중앙화
    private void handleAuthException(Exception e) {
        if (e instanceof ExpiredJwtException) {
            throw new CustomAuthException(CustomAuthException.AuthErrorType.TOKEN_EXPIRED,
                    "만료된 토큰: " + ((ExpiredJwtException) e).getClaims().getExpiration());
        } else if (e instanceof MalformedJwtException || e instanceof IllegalArgumentException) {
            throw new CustomAuthException(CustomAuthException.AuthErrorType.MALFORMED_TOKEN,
                    "토큰 구조 오류: " + e.getMessage());
        } else if (e instanceof UnsupportedJwtException) {
            throw new CustomAuthException(CustomAuthException.AuthErrorType.UNSUPPORTED_TOKEN,
                    "지원하지 않는 토큰 형식: " + e.getMessage());
        } else if (e instanceof SecurityException) {
            throw new CustomAuthException(CustomAuthException.AuthErrorType.CREDENTIALS_MISMATCH,
                    "토큰 정보 불일치: " + e.getMessage());
        }
    }
}
