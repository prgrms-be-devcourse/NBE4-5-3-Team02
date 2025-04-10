package com.snackoverflow.toolgether.domain.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Transactional
class UserInfoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserServiceV2 userService;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @Autowired
    EntityManager entityManager;

    @Autowired
    PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        testUser = userRepository.findByEmail("test@example.com");

        CustomUserDetails userDetails = new CustomUserDetails(1L, "test@example.com");
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                )
        );
    }

    @Test
    @DisplayName("휴대폰 번호 인증으로 이메일을 찾는다")
    void find_email() throws Exception {
        //given:
        String expectedEmail = "user1@example.com";

        // when & then
        mockMvc.perform(post("/api/v2/users/find-email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .param("phoneNumber", "01012345678"))
                .andExpect(status().isOk()) // HTTP 200 상태 코드 검증
                .andExpect(jsonPath("$.resultCode").value("200")) // 응답 코드 검증
                .andExpect(jsonPath("$.msg").value("고객님의 이메일: " + expectedEmail)) // 응답 메시지 검증
                .andExpect(jsonPath("$.data").value(expectedEmail)); // 반환된 이메일 검증
    }

    static class PasswordRequest {
        String password;

        public PasswordRequest(String password) {
            this.password = password;
        }

        public String getPassword() {
            return password;
        }
    }

    @Test
    @DisplayName("비밀번호를 변경한다")
    void change_password_success() throws Exception {
        //given: 시큐리티 객체 생성
        PasswordRequest newPassword = new PasswordRequest("newPassword123");

        //when & then
        mockMvc.perform(post("/api/v2/users/change-password")
        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newPassword))
                        .header("X-Test-Auth", "test@example.com"))
                .andExpect(status().isOk()) // HTTP 200 상태 코드 검증
                .andExpect(jsonPath("$.resultCode").value("200")) // 응답 코드 검증
                .andExpect(jsonPath("$.msg").value("비밀번호 변경에 성공했습니다")) // 응답 메시지 검증
                .andExpect(jsonPath("$.data").isEmpty()); // 반환 데이터가 없는지 확인
    }

    @Test
    @DisplayName("이전과 비밀번호가 동일할 경우 변경하지 않는다")
    void change_password_fail() throws Exception {
        //given: 시큐리티 객체 생성
        PasswordRequest newPassword = new PasswordRequest("password");

        String encoded = passwordEncoder.encode("password");

        //when & then
        mockMvc.perform(post("/api/v2/users/change-password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(encoded))
                        .header("X-Test-Auth", "test@example.com"))
                .andExpect(status().isBadRequest());
    }
}