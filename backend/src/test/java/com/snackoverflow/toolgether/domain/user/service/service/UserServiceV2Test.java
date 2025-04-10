package com.snackoverflow.toolgether.domain.user.service.service;

import com.snackoverflow.toolgether.domain.user.dto.v2.SignupRequestV2;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.domain.user.service.UserServiceV2;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class UserServiceV2Test {

    @Autowired
    private UserServiceV2 userService;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("회원가입 성공 시 사용자 ID가 반환되어야 한다")
    void registerUser_ShouldReturnUserId() {
        // Given: 회원가입 요청 객체 생성
        SignupRequestV2 request = new SignupRequestV2(
                "test@signUp.com", // 이메일
                "password123",      // 비밀번호
                "password123",      // 비밀번호 확인
                "nickname123",      // 닉네임
                "01000001111",    // 전화번호
                37.5665,            // 위도 (서울)
                127.0303            // 경도 (성동구)
        );

        // When: 회원가입 메서드 호출
        Long userId = userService.registerUser(request);

        // Then: 결과 검증
        assertNotNull(userId); // 반환된 사용자 ID가 null이 아니어야 함

        // 저장된 사용자 검증
        User savedUser = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        assertEquals("test@signUp.com", savedUser.getEmail());
        assertEquals("서울 성동구", savedUser.getBaseAddress());
        assertEquals("nickname123", savedUser.getNickname());
        assertEquals("01000001111", savedUser.getPhoneNumber());

        System.out.println(savedUser.getId());
        System.out.println(savedUser.getEmail());
        System.out.println(savedUser.getBaseAddress());
    }
}

