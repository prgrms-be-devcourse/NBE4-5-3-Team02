package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.v2.SignupRequestV2;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static com.snackoverflow.toolgether.global.exception.ErrorCode.*;


/**
 * 휴대폰 인증, 좌표 -> 주소 변환, 추가 정보를 번호만 받도록 변경
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceV2 {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final LocationService locationService;

    // 이메일 중복 방지 -> 실시간으로 중복 확인을 하기 위해서 메서드 분리
    public void checkEmailDuplicate(String email) {
        if (userRepository.existsByEmail(email)) {
            throw new ServiceException(DUPLICATE_FIELD);
        }
    }

    // 닉네임 중복 방지
    public void checkNicknameDuplicate(String nickname) {
        if (userRepository.existsByNickname(nickname)) {
            throw new ServiceException(DUPLICATE_FIELD);
        }
    }

    // 비밀번호 확인 필드
    public void checkPassword(String password, String confirmPassword) {
        if (!password.equals(confirmPassword)) {
            throw new ServiceException(PASSWORD_MISMATCH);
        }
    }

    // 일반 회원 가입 (역지오코딩)
    @Transactional
    public Long registerUser(SignupRequestV2 request) {
        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        // 위도, 경도를 받아서 주소로 변환하는 로직 추가
        String address = locationService.convertCoordinateToAddress(request.getLatitude(), request.getLongitude());

        User user = User.createGeneralUser(
                request.getEmail(),
                encodedPassword,
                request.getPhoneNumber(),
                request.getNickname(),
                address);

        userRepository.save(user);

        return user.getId();
    }

    // 사용자 로그인 -> 이메일, 패스워드가 맞는지 검증
    public void authenticateUser(String email, String password) {
        User user = userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ServiceException(PASSWORD_MISMATCH);
        }
    }

    public void checkBeforePassword(Long id, String newPassword) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new ServiceException(SAME_PASSWORD);
        }
    }

    @Transactional
    public void changePassword(Long id, String password) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        String updatePassword = passwordEncoder.encode(password);
        user.updatePassword(updatePassword);
    }

    // 휴대폰 번호로 유저 이메일 찾기
    public String getUserEmail(String phoneNumber) {
        return userRepository.findByphoneNumber(phoneNumber).orElseThrow(UserNotFoundException::new).getEmail();
    }

    // 프로필 가져오기
    public String getMyProfile(Long userId) {
        User user = userRepository.findById(userId).orElseThrow((UserNotFoundException::new));
        return user.getProfileImage() == null ? "" : user.getProfileImage();
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(UserNotFoundException::new);
    }

    public User getUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    public boolean existsUser(String email) {
        return userRepository.existsByEmail(email);
    }
}
