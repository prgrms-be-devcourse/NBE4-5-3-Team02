package com.snackoverflow.toolgether.global.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.token.JwtService;
import com.snackoverflow.toolgether.global.token.TokenService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.Cookie;
import nl.altindag.log.LogCaptor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockFilterChain;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.snackoverflow.toolgether.global.constants.AppConstants.REFRESH_TOKEN;
import static com.snackoverflow.toolgether.global.constants.AppConstants.REMEMBER_ME_TOKEN;
import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ExtendWith(OutputCaptureExtension.class)
@ActiveProfiles("local")
class CustomAuthenticationFilterTest {

    @Value("${jwt.refresh_expiration}")

    private Long refresh;

    @Value("${jwt.rememberMe_expiration}")
    private Long rememberMe;

    @Autowired
    private UserRepository userRepository; // 필터에서는 최대한 DB 에 접근하지 않도록
    @Autowired
    private JwtService jwtService;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CustomAuthenticationFilter customFilter;

    private static String testToken;

    @BeforeEach
    void setup() {
        customFilter = new CustomAuthenticationFilter(objectMapper, userRepository, tokenService, jwtService);
        testToken = tokenService.createTokenByEmailAndId("test@example.com", 1L, rememberMe);
    }

    // 테스트용 컨트롤러
    @RestController
    static class TestController {
        @GetMapping("/test")
        public ResponseEntity<String> test(@Login CustomUserDetails userDetails) {
            String email = userDetails.getUserEmail();
            Long userId = userDetails.getUserId();
            return ResponseEntity.status(200).body("사용자 정보: " + email + ", " + userId);
        }
    }

    @Test
    @DisplayName("토큰 생성과 파싱 검증")
    void createTokenTest() throws Exception {
        Claims claims = jwtService.parseAndValidateToken(testToken);
        String email = (String) claims.get("email");
        Long userId = ((Integer) claims.get("userId")).longValue();

        assertThat(email).isEqualTo("test@example.com");
        assertThat(userId).isEqualTo(1L);
    }

    @Test
    @DisplayName("Remember-Me 토큰으로 검증 테스트")
    void testRememberMe() throws Exception {
        // given: 테스트용 유저 설정 & 요청에 쿠키 추가
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(REMEMBER_ME_TOKEN, testToken));
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // when: 필터 실행
        customFilter.doFilter(request, response, filterChain);

        // then: 인증 객체 생성
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        assertThat(authentication.getPrincipal()).isNotNull();
        assertThat(authentication.getPrincipal()).isInstanceOf(CustomUserDetails.class);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        assertThat(userDetails.getUserId()).isEqualTo(1L);
        assertThat(userDetails.getUserEmail()).isEqualTo("test@example.com");
        // 이후 만료 시간 설정이 안 되었다고 뜨나 실제로 돌렸을 때에는 문제 발생하지 않음 (Mock 이라 그런 듯함)
        // 인증 헤더, 리프레시 토큰으로 제대로 발급됨을 확인
    }

    @Test
    @DisplayName("액세스 토큰 만료, 리프레시 토큰으로 재발급")
    void newAccessTokenTest(CapturedOutput output) throws Exception {
        // given: 일부러 만료 시간을 줄인 액세스 토큰을 생성
        String accessToken  = tokenService.createTokenByEmailAndId("test@example.com", 1L, 1000L);
        String refreshToken  = tokenService.createTokenByEmailAndId("test@example.com", 1L, refresh);

        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(REFRESH_TOKEN, refreshToken));
        request.addHeader("Authorization", "Bearer " + accessToken);
        MockHttpServletResponse response = new MockHttpServletResponse();
        MockFilterChain filterChain = new MockFilterChain();

        // 액세스 토큰이 만료될 수 있도록 잠시 대기
        Thread.sleep(2000);

        // when: 필터 실행
        LogCaptor logCaptor = LogCaptor.forClass(CustomAuthenticationFilter.class);
        customFilter.doFilter(request, response, filterChain);

        // then: 재발급 로직이 실행되는지 검증
        assertThat(logCaptor.getInfoLogs()).contains("토큰 만료, 재발급 로직 시작");
    }
}