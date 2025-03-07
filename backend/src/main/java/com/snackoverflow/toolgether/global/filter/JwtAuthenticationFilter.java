package com.snackoverflow.toolgether.global.filter;

import com.snackoverflow.toolgether.global.exception.custom.mail.CustomAuthException;
import com.snackoverflow.toolgether.global.util.JwtUtil;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Optional;

import static com.snackoverflow.toolgether.global.exception.custom.mail.CustomAuthException.AuthErrorType.MALFORMED_TOKEN;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        try {
            // 쿠키에서 JWT 토큰 추출
            Optional<String> token = jwtUtil.getJwtFromCookies(request);
            log.info("JWT Token={}", token.orElse("No Token Found"));

            if (token.isPresent()) {
                // 페이로드 조회
                Claims claims = jwtUtil.getPayload(token.get());
                log.info("JWT claims={}", claims);

                // 사용자 정보 추출
                String username = (String) claims.get("username"); // 사용자의 아이디
                String email = (String) claims.get("email");
                log.info("token -> username: {}", username);
                log.info("token -> email: {}", email);

                // 인증 객체 생성 및 저장
                CustomUserDetails customUserDetails = new CustomUserDetails(username, email);
                Authentication authentication = new UsernamePasswordAuthenticationToken(customUserDetails,
                        null, Collections.emptyList());
                log.info("authentication={}", authentication);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            // 토큰이 없어도 게시물은 조회할 수 있도록
            filterChain.doFilter(request, response);
        }
    }
}
