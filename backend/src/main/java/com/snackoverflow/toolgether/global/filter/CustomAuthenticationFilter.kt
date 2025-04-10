package com.snackoverflow.toolgether.global.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.ErrorCode;
import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import com.snackoverflow.toolgether.global.token.JwtService;
import com.snackoverflow.toolgether.global.token.TokenService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts.claims
import io.lettuce.core.KillArgs.Builder.user
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction.authentication
import org.springframework.security.web.http.SecurityHeaders.bearerToken
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static
import sun.jvm.hotspot.HelloWorld.e

com.snackoverflow.toolgether.global.constants.AppConstants.REFRESH_TOKEN;
import static com.snackoverflow.toolgether.global.constants.AppConstants.REMEMBER_ME_TOKEN;

/**
 * 사용자 검증 필터
 * access token -> header
 * refresh token -> cookie (session)
 * <p>
 * remember me token -> 지속, 브라우저 꺼도 쿠키는 남아있음
 * 유저 객체를 검증하고 로그인 페이지 클릭 시에 자동 로그인 처리
 * <p>
 * 소셜 로그인 -> 유저 객체 검증 및 정보를 가져온 후, jwt 토큰 발급
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.access_expiration}")
    private Long access;

    @Value("${jwt.refresh_expiration}")
    private Long refresh;

    private final UserRepository userRepository; // 필터에서는 최대한 DB 에 접근하지 않도록
    private final JwtService jwtService;
    private final TokenService tokenService;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws IOException {
        log.info("CustomAuthenticationFilter 실행");

        try {
            // 테스트 모드 활성화: 헤더에 "X-Test-Auth"가 존재하면 강제 인증 처리
            String testAuthHeader = request.getHeader("X-Test-Auth");
            if (StringUtils.hasText(testAuthHeader)) {
                // 테스트용 사용자 정보 생성
                User testUser = userRepository.findByEmail(testAuthHeader).orElseThrow(UserNotFoundException::new);
                setAuthentication(testUser.getEmail(), testUser.getId());
                filterChain.doFilter(request, response); // 필터 체인 계속 진행
                return;
            }

            // JWT 토큰 처리
            log.info("header 검증 시작");
            String authorizationHeader = request.getHeader("Authorization");
            log.info("authorizationHeader: {}", authorizationHeader);
            if (authorizationHeader != null) {
                String accessToken = extractTokenFromHeader(request);
                log.info("accessToken 추출: {}", accessToken);
                if (jwtService.isValidToken(accessToken)) {
                    Claims claims = jwtService.parseAndValidateToken(accessToken);
                    if (claims != null) {
                        handleJwt(claims);  // 파싱된 Claims 전달
                        filterChain.doFilter(request, response); // 필터 체인 계속 진행
                        return; // 체인 종료
                    }
                } else {
                    log.info("토큰 만료, 재발급 로직 시작");
                    String newAccessToken = getNewAccessToken(request);
                    response.setHeader("Authorization", "Bearer " + newAccessToken); // 재발급 후 다시 헤더에 넣어줄 것!
                    Claims newClaims = jwtService.parseAndValidateToken(newAccessToken);
                    handleJwt(newClaims);  // 새로운 토큰의 Claims 전달
                    filterChain.doFilter(request, response); // 필터 체인 계속 진행
                    return; // 체인 종료
                }
            }

            // Remember-Me 토큰 처리
            Optional<String> rememberMeToken = jwtService.getTokenByCookieName(request, REMEMBER_ME_TOKEN);
            if (rememberMeToken.isPresent()) {
                checkBlackList(rememberMeToken);

                Claims claims = jwtService.parseAndValidateToken(rememberMeToken.get());
                String email = (String) claims.get("email");
                Long userId = ((Integer) claims.get("userId")).longValue();

                setAuthentication(email, userId);
                saveNewTokens(response, userId, email);

                // 자동 로그인 응답 작성
                User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
                response.setStatus(HttpServletResponse.SC_OK); // 200 OK
                response.getWriter().write(getResponse(response, user));
                filterChain.doFilter(request, response); // 필터 체인 계속 진행
                return; // 여기서 체인 종료
            }

            filterChain.doFilter(request, response); // 최종적으로 항상 체인 계속 진행

        } catch (ExpiredJwtException e) {
            String newAccessToken = getNewAccessToken(request);
            if (newAccessToken != null) {
                Claims newClaims = jwtService.parseAndValidateToken(newAccessToken);
                if (newClaims != null) {
                    handleJwt(newClaims);  // 새로운 토큰의 Claims 전달
                } else {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
                    response.getWriter().write("인증되지 않은 사용자입니다.");
                }
            }
        } catch (UserNotFoundException e) {
            log.error("유저를 찾을 수 없음: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_NOT_FOUND); // 404 Not Found
            response.getWriter().write("사용자를 찾을 수 없음");
        } catch (ServiceException e) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            response.getWriter().write("인증되지 않은 사용자입니다.");
        } catch (Exception e) {
            log.error("내부 서버 오류: {}", e.getMessage());
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR); // 500 Internal Server Error
            response.getWriter().write("서버 오류 발생");
        }
    }

    // 자동 로그인 처리 -> 응답 객체 생성
    private String getResponse(HttpServletResponse response, User user) throws JsonProcessingException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "자동 로그인 성공");
        responseBody.put("status", 200);

        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("user_id", user.getId());
        userInfo.put("nickname", user.getNickname());

        responseBody.put("user", userInfo);

        // Jackson ObjectMapper로 JSON 변환
        return objectMapper.writeValueAsString(responseBody);
    }

    // 자동 로그인 사용자 -> 액세스 토큰과 리프레시 토큰을 자동으로 발급
    private void saveNewTokens(HttpServletResponse response, Long userId, String email) {
        String accessToken = tokenService.createTokenByEmailAndId(email, userId, access);
        response.setHeader("Authorization", "Bearer " + accessToken); // accessToken 을 header 에 저장
        String refreshToken = tokenService.createTokenByEmailAndId(email, userId, refresh);
        jwtService.setJwtSessionCookie(refreshToken, response); // refreshToken 을 cookie 에 저장
        log.info("remember me: 새로운 토큰 설정, accessToken: {}, refreshToken: {}", accessToken, refreshToken);
    }

    // 액세스 토큰 만료 시 자동으로 재발급
    private String getNewAccessToken(HttpServletRequest request) {
        Optional<String> refreshToken = jwtService.getTokenByCookieName(request, REFRESH_TOKEN);
        if (refreshToken.isPresent()) {
            checkBlackList(refreshToken);

            Claims claims = jwtService.parseAndValidateToken(refreshToken.get());
            String email = (String) claims.get("email");
            Long userId = ((Integer) claims.get("userId")).longValue();
            return tokenService.createTokenByEmailAndId(email, userId, access);
        }
        return null;
    }

    private void checkBlackList(Optional<String> token) {
        // 블랙리스트 확인
        log.info("검증할 토큰: {}", token.get());
        if (tokenService.isTokenBlacklisted(token.get())) {
            throw new ServiceException(ErrorCode.IN_BLACKLIST);
        }
    }

    private String extractTokenFromHeader(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7); // "Bearer " 이후의 토큰 부분만 반환
        }
        return null;
    }

    private void handleJwt(Claims claims) {
        String email = (String) claims.get("email");
        Long userId = ((Integer) claims.get("userId")).longValue();
        setAuthentication(email, userId); // 인증 객체 설정
        log.info("인증 객체가 설정되었습니다. 사용자 ID: {}", userId);
    }

    private void setAuthentication(String email, Long userId) {
        // 인증 객체에는 최소한의 정보만 담음 (id, 이메일, 권한)
        CustomUserDetails customUserDetails = new CustomUserDetails(userId, email);

        // 인증 완료 후 민감 데이터 제거를 위해 Credentials 필드를 null 로 처리
        Authentication authentication = new UsernamePasswordAuthenticationToken(
                customUserDetails, null, customUserDetails.getAuthorities());

        SecurityContextHolder.getContext().setAuthentication(authentication);
        log.info("인증 성공: {}", authentication);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String requestURI = request.getRequestURI();

        // 필터링 제외 조건 통합
        return requestURI.startsWith("/h2-console") ||
                requestURI.startsWith("/login/oauth2/code/google") ||
                requestURI.startsWith("/api/v2/users/") ||
                requestURI.startsWith("/chat") ||
                requestURI.matches(".*\\.(css|js|gif|png|jpg|ico)$");
    }
}
