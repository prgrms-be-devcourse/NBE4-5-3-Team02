package com.snackoverflow.toolgether.domain.user.controller;

import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.domain.user.service.MessageService;
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2;
import com.snackoverflow.toolgether.global.token.JwtService;
import com.snackoverflow.toolgether.global.token.TokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
@Transactional
class UserControllerV2Test {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserServiceV2 userService;

    @MockitoBean
    private MessageService messageService;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private TokenService tokenService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;


    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("userControllerTest@example.com");
    }

    @Test
    @DisplayName("회원가입 성공")
    void signup_success() throws Exception {
        //given: 테스트 데이터 준비
        String requestBody = """
                {
                    "email": "signuptest@test.com",
                    "password": "password123",
                    "checkPassword": "password123",
                    "nickname": "signupTestUser",
                    "phoneNumber": "01088887777",
                    "latitude": 37.5665,
                    "longitude": 126.9780
                }
                """;

        // mocking: messageService의 동작 정의 -> 서비스 객체만 단독 테스트 진행했으니 mock 설정에 문제 없음
        when(messageService.isVerified("01088887777")).thenReturn(true); // 휴대폰 인증 성공

        //when & then
        mockMvc.perform(post("/api/v2/users/signup")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody)) // JSON 요청 본문 설정
                .andExpect(status().isOk()) // HTTP 200 상태 코드 검증
                .andExpect(jsonPath("$.resultCode").value("201")) // 응답 코드 검증
                .andExpect(jsonPath("$.msg").value("회원 가입에 성공하였습니다.")); // 응답 메시지 검증

        assertThat(userRepository.findByEmail("signuptest@test.com")).isNotNull();
        assertThat(userRepository.findByEmail("signuptest@test.com").getBaseAddress()).isEqualTo("서울 중구");
    }

    @Test
    @DisplayName("로그인 성공")
    void login_success() throws Exception {

        String encoded = passwordEncoder.encode("password");
        testUser = User.createGeneralUser(
                "userControllerTest@example.com",
                encoded,
                "01088882222",
                "userControllerTest",
                "서울 성동구");
        userRepository.save(testUser);
        // given: 요청 본문 생성
        String requestBody = """
                {
                    "email": "userControllerTest@example.com",
                    "password": "password",
                    "rememberMe": false
                }
                """;

        ResultActions resultActions = mockMvc
                .perform(
                        post("/api/v2/users/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody)
                )
                .andDo(print());

        resultActions.andExpect(status().isOk());
        resultActions.andExpect(jsonPath("$.msg").value("로그인에 성공하였습니다."));
        resultActions.andExpect(jsonPath("$.data").exists());
    }

    @Test
    @DisplayName("로그아웃 후 토큰이 사라지는지 검증한다")
    void logout_tokenAddBlackList() throws Exception {
        //given: 토큰 설정 (액세스 토큰은 만료 시간이 짧아 블랙리스트에 추가하지 않는다)
        String refreshToken = "testRefreshToken";
        String rememberMeToken = "testRememberMeToken";

        //when: 로그아웃 요청 실행
        ResultActions result = mockMvc.perform(get("/api/v2/logout")
                .header("X-Test-Auth", "test@example.com")
                .cookie(new Cookie("refresh_token", refreshToken))
                .cookie(new Cookie("remember_me_token", rememberMeToken)));

        // then: 응답 검증
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-3"))
                .andExpect(jsonPath("$.msg").value("로그아웃에 성공하였습니다."))
                // 쿠키 검증: refresh_token의 값이 null인지 확인
                .andExpect(cookie().value("refresh_token", (String) null))
                // 쿠키 검증: remember_me_token의 값이 null인지 확인
                .andExpect(cookie().value("remember_me_token", (String) null));
    }
}

