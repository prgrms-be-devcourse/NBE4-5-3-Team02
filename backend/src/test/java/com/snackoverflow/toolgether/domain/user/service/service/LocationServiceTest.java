package com.snackoverflow.toolgether.domain.user.service.service;

import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.domain.user.service.LocationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class LocationServiceTest {

    @Autowired
    private LocationService locationService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        String encoded = passwordEncoder.encode("password");
        testUser = User.createGeneralUser(
                "test@example.com",
                encoded,
                "01020203030",
                "testUser",
                "서울 성동구");
        userRepository.save(testUser);
        testUser = userRepository.findByEmail("test@example.com");
    }

    @Test
    @DisplayName("좌표를 주소로 변환 - 실제 Kakao API 호출한다")
    void convertCoordinateToAddress_ShouldReturnAddress() {
        // Given: 위도와 경도 값 설정 (서울 시청 좌표)
        double latitude = 37.5665;
        double longitude = 126.9780;

        // When: 메서드 실행
        String address = locationService.convertCoordinateToAddress(latitude, longitude);

        // Then: 변환된 주소 검증
        assertNotNull(address);
        assertTrue(address.contains("서울")); // 예: "서울 중구" 형태의 주소 반환
        System.out.println("변환된 주소: " + address);
    }
}