package com.snackoverflow.toolgether.global.filter;

import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.domain.user.service.OauthService;
import com.snackoverflow.toolgether.global.exception.custom.user.UserNotFoundException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
// 헤더 기반 OAuth2 액세스 토큰 처리를 담당
public class GoogleAccessTokenFilter extends OncePerRequestFilter {

    private final OauthService oauthService;
    private final UserRepository userRepository;


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        log.info("GoogleAccessTokenFilter 실행됨");
        String authHeader = request.getHeader("Authorization");

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String accessToken = authHeader.substring(7);// "Bearer " 제거
            log.info("accessToken = {}", accessToken);
            try {
                Map<String, Object> userInfo = oauthService.getUserInfo(accessToken);
                String email = (String) userInfo.get("email");
                User user = userRepository.findByEmail(email).orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

                CustomUserDetails customUserDetails = new CustomUserDetails(user.getUsername(), email, user.getId());
                Authentication authentication = new UsernamePasswordAuthenticationToken(
                        customUserDetails, null, Collections.emptyList());

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("authentication = {}", authentication);
                filterChain.doFilter(request, response);
            } catch (RuntimeException e) {
                log.error("OAuth2 Access Token 처리 중 오류 발생: {}", e.getMessage());
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("액세스 토큰이 유효하지 않습니다.");
            }
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String authHeader = request.getHeader("Authorization");
        return authHeader == null || !authHeader.startsWith("Bearer "); // Authorization 헤더가 없는 경우 제외
    }
}
