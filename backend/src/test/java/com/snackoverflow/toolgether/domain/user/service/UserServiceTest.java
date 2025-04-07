package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.MeInfoResponse;
import com.snackoverflow.toolgether.domain.user.dto.request.PatchMyInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.util.JwtUtil;
import com.snackoverflow.toolgether.global.util.s3.S3Service;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private VerificationService verificationService;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private WebClient webClient;

    @Mock
    private OauthService oauthService;

    @Mock
    private S3Service s3Service;

    @InjectMocks
    private UserService userService;

    private User testUser;
    private PatchMyInfoRequest testRequest;
    private MockMultipartFile testImage;

    @BeforeEach
    void setUp() {
        testUser = new User(
                1L,
                "human123",
                null,
                "test1@gmail.com",
                null,
                null,
                "000-0000-0001",
                "닉네임1",
                new Address("서울시 강남구", "역삼동 123-45", "12345"),
                LocalDateTime.now(),
                35.1, // Corrected latitude
                129.0, // Corrected longitude
                true,
                null,
                30,
                0,
                null
        );

        testImage = new MockMultipartFile("profileImage", "test.png", "image/png", "test image".getBytes());
        testRequest = new PatchMyInfoRequest();
        testRequest.setNickname("newNickname");
        testRequest.setPhoneNumber("01098765432");
        testRequest.setAddress(new Address("부산", "해운대", "54321"));
        testRequest.setLatitude(35.1);
        testRequest.setLongitude(129.0);
    }

    @Test
    @DisplayName("getMeInfo 테스트")
    void getMeInfoTest() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(testUser));
        MeInfoResponse meInfoResponse = userService.getMeInfo(1L);
        assertEquals(1L, meInfoResponse.id());
        assertEquals("닉네임1", meInfoResponse.nickname());
        assertEquals("human123", meInfoResponse.username());
        assertNull(meInfoResponse.profileImage());
        assertEquals("test1@gmail.com", meInfoResponse.email());
        assertEquals("000-0000-0001", meInfoResponse.phoneNumber());
        assertEquals("서울시 강남구", meInfoResponse.address().mainAddress());
        assertEquals("역삼동 123-45", meInfoResponse.address().detailAddress());
        assertEquals("12345", meInfoResponse.address().zipcode());
        assertEquals(35.1, meInfoResponse.latitude());
        assertEquals(129.0, meInfoResponse.longitude());
        assertNotNull(meInfoResponse.createdAt());
        assertEquals(30, meInfoResponse.score());
        assertEquals(0, meInfoResponse.credit());
        verify(userRepository, times(1)).findById(1L);
    }

//    @Test
//    @DisplayName("findUserById 테스트 - 사용자 존재")
//    void findUserById_UserExists() {
//        // Given
//        User mockFoundUser = Mockito.mock(User.class);
//        Mockito.when(mockFoundUser.getUsername()).thenReturn("human123");
//
//        when(userRepository.findById(1L)).thenReturn(Optional.of(mockFoundUser));
//
//        // When
//        User foundUser = userService.findUserById(1L);
//
//        // Then
//        assertEquals("human123", foundUser.getUsername());
//        verify(userRepository, times(1)).findById(1L); // 명시적으로 Long 타입으로 캐스팅
//    }

    @Test
    @DisplayName("findUserById 테스트 - 사용자 없음")
    void findUserById_UserNotFound() {
        // Given
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        // When
        assertThrows(RuntimeException.class, () -> userService.findUserById(1L));
    }

    @Test
    @DisplayName("postProfileImage 테스트")
    void postProfileImageTest() {
        when(s3Service.upload(any(), any())).thenReturn("http://s3.amazonaws.com/test.png");
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.postProfileImage(testUser, testImage);

        assertEquals("http://s3.amazonaws.com/test.png", testUser.getProfileImage());
        verify(s3Service, times(1)).upload(any(), any());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("deleteProfileImage 테스트 - 기존 이미지 URL 있음")
    void deleteProfileImageTest_ExistingImageUrl() {
        testUser.updateProfileImage("http://s3.amazonaws.com/dirName/old.png"); // Adjusted URL
        when(userRepository.save(any(User.class))).thenReturn(testUser);
        doNothing().when(s3Service).delete("http://s3.amazonaws.com/dirName/old.png"); // Expecting full URL

        userService.deleteProfileImage(testUser);

        assertNull(testUser.getProfileImage());
        verify(s3Service, times(1)).delete("http://s3.amazonaws.com/dirName/old.png");
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("deleteProfileImage 테스트 - 기존 이미지 URL 없음")
    void deleteProfileImageTest_NoExistingImageUrl() {
        // Given
        User mockTestUser = Mockito.mock(User.class);
        Mockito.when(mockTestUser.getProfileImage()).thenReturn(null); // 기존 이미지 URL이 없음을 설정

        // When
        userService.deleteProfileImage(mockTestUser);

        // Then
        verify(mockTestUser, times(0)).deleteProfileImage(); // profileImage가 null이므로 deleteProfileImage()는 호출되지 않아야 함
        verify(s3Service, times(0)).delete(any()); // profileImage가 null이므로 s3Service.delete()는 호출되지 않아야 함
        verify(userRepository, times(0)).save(any()); // profileImage가 null이므로 userRepository.save()는 호출되지 않아야 함
    }

    @Test
    @DisplayName("deleteUser 테스트 - 삭제 상태 확인")
    void deleteUserTest_CheckDeletionStatus() {
        // Given
        User mockTestUser = Mockito.mock(User.class); // 로컬 Mock 객체 생성
        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        LocalDateTime deletedAt = LocalDateTime.now();

        // When
        userService.deleteUser(mockTestUser); // 로컬 Mock 객체를 서비스 메서드에 전달

        // Then
        verify(mockTestUser, times(1)).delete(); // Mock 객체의 delete() 메서드가 호출되었는지 확인
        when(mockTestUser.getDeletedAt()).thenReturn(deletedAt); // delete() 호출 후 getDeletedAt()이 특정 LocalDateTime 객체를 반환하도록 스텁 처리
        verify(userRepository, times(1)).save(userCaptor.capture()); // userRepository.save()가 호출되었는지 확인하고 인수를 캡처

        User capturedUser = userCaptor.getValue();
        assertEquals(deletedAt, capturedUser.getDeletedAt()); // 캡처된 User 객체의 deletedAt이 스텁 처리한 값과 동일한지 확인
    }

    @Test
    @DisplayName("checkMyInfoDuplicates 테스트 - 닉네임 중복")
    void checkMyInfoDuplicates_NicknameDuplicate() {
        Address address = new Address("테스트시", "테스트구", "12345");
        User duplicateUser = new User(
                2L,
                "duplicateUser",
                null,
                "duplicate@example.com",
                null,
                null,
                "01011112222",
                "중복된닉네임",
                address,
                LocalDateTime.now(),
                0.0,
                0.0,
                true,
                null,
                30,
                0,
                null
        );
        when(userRepository.findByNickname("newNickname")).thenReturn(duplicateUser);
        String result = userService.checkMyInfoDuplicates(testUser, testRequest);
        assertEquals("닉네임", result);
        verify(userRepository, times(1)).findByNickname("newNickname");
        verify(userRepository, times(0)).findByPhoneNumber(any());
    }

    @Test
    @DisplayName("checkMyInfoDuplicates 테스트 - 전화번호 중복")
    void checkMyInfoDuplicates_PhoneNumberDuplicate() {
        Address address = new Address("테스트시", "테스트구", "12345");
        User duplicateUser = new User(
                2L,
                "duplicateUser",
                null,
                "duplicate@example.com",
                null,
                null,
                "01098765432",
                "중복된닉네임",
                address,
                LocalDateTime.now(),
                0.0,
                0.0,
                true,
                null,
                30,
                0,
                null
        );
        when(userRepository.findByNickname("newNickname")).thenReturn(null);
        when(userRepository.findByPhoneNumber("01098765432")).thenReturn(duplicateUser);
        String result = userService.checkMyInfoDuplicates(testUser, testRequest);
        assertEquals("전화번호", result);
        verify(userRepository, times(1)).findByNickname("newNickname");
        verify(userRepository, times(1)).findByPhoneNumber("01098765432");
    }

    @Test
    @DisplayName("checkMyInfoDuplicates 테스트 - 중복 없음")
    void checkMyInfoDuplicates_NoDuplicate() {
        when(userRepository.findByNickname("newNickname")).thenReturn(null);
        when(userRepository.findByPhoneNumber("01098765432")).thenReturn(null);
        String result = userService.checkMyInfoDuplicates(testUser, testRequest);
        assertEquals("", result);
        verify(userRepository, times(1)).findByNickname("newNickname");
        verify(userRepository, times(1)).findByPhoneNumber("01098765432");
    }

    @Test
    @DisplayName("checkMyInfoDuplicates 테스트 - 자신의 닉네임과 동일")
    void checkMyInfoDuplicates_SameNickname() {
        // Given
        testRequest.setNickname("닉네임1"); // 요청 닉네임을 현재 사용자 닉네임과 동일하게 설정
        testRequest.setPhoneNumber("01098765432"); // 요청 전화번호는 다르게 설정

        when(userRepository.findByNickname("닉네임1")).thenReturn(testUser); // 요청 닉네임으로 조회 시 현재 사용자를 반환하도록 Mock
        when(userRepository.findByPhoneNumber("01098765432")).thenReturn(null); // 요청 전화번호로 조회 시 사용자가 없다고 가정

        // When
        String result = userService.checkMyInfoDuplicates(testUser, testRequest);

        // Then
        assertEquals("", result); // 중복이 없어야 함
        verify(userRepository, times(1)).findByNickname(any()); // 1번 호출될 것으로 예상
        verify(userRepository, times(1)).findByPhoneNumber(any()); // 1번 호출될 것으로 예상
    }

    @Test
    @DisplayName("checkMyInfoDuplicates 테스트 - 자신의 전화번호와 동일")
    void checkMyInfoDuplicates_SamePhoneNumber() {
        // Given
        testRequest.setPhoneNumber("000-0000-0001");
        testRequest.setNickname("newNickname");

        when(userRepository.findByNickname("newNickname")).thenReturn(null);
        when(userRepository.findByPhoneNumber("000-0000-0001")).thenReturn(null); // Mock 필요

        // When
        String result = userService.checkMyInfoDuplicates(testUser, testRequest);

        // Then
        assertEquals("", result);
        verify(userRepository, times(1)).findByNickname(any());
        verify(userRepository, times(1)).findByPhoneNumber(any()); // 1번 호출될 것으로 예상
    }


    @Test
    @DisplayName("updateMyInfo 테스트")
    void updateMyInfoTest() {
        when(userRepository.save(any(User.class))).thenReturn(testUser);

        userService.updateMyInfo(testUser, testRequest);

        assertEquals("newNickname", testUser.getNickname());
        assertEquals("01098765432", testUser.getPhoneNumber());
        assertEquals("부산", testUser.getAddress().getMainAddress());
        assertEquals("해운대", testUser.getAddress().getDetailAddress());
        assertEquals("54321", testUser.getAddress().getZipcode());
        assertEquals(35.1, testUser.getLatitude());
        assertEquals(129.0, testUser.getLongitude());
        verify(userRepository, times(1)).save(testUser);
    }

    @Test
    @DisplayName("getUserForUsername 테스트 - 사용자 존재")
    void getUserForUsername_UserExists() {
        when(userRepository.findByUsername("human123")).thenReturn(Optional.of(testUser));
        User foundUser = userService.getUserForUsername("human123");
        assertEquals("human123", foundUser.getUsername());
        verify(userRepository, times(1)).findByUsername("human123");
    }

    @Test
    @DisplayName("getUserForUsername 테스트 - 사용자 없음")
    void getUserForUsername_UserNotFound() {
        when(userRepository.findByUsername("nonExistentUser")).thenReturn(Optional.empty());
        assertThrows(RuntimeException.class, () -> userService.getUserForUsername("nonExistentUser"));
        verify(userRepository, times(1)).findByUsername("nonExistentUser");
    }
}