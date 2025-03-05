package com.snackoverflow.toolgether.domain.user.service;

import org.springframework.transaction.annotation.Transactional;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.dto.SignupRequest;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.custom.duplicate.DuplicateFieldException;
import com.snackoverflow.toolgether.global.exception.custom.mail.VerificationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;

    // 이메일, 아이디, 닉네임 중복 방지
    public void checkDuplicates(SignupRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateFieldException("사용자 ID 중복 오류 발생");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateFieldException("사용자 EMAIL 중복 오류 발생");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateFieldException("사용자 닉네임 중복 오류 발생");
        }
    }

    // 회원 가입
    public void registerVerifiedUser(SignupRequest request) {
        // 이메일 인증 완료 시 회원 가입 허용
        if (!verificationService.isEmailVerified(request.getEmail())) {
            throw new VerificationException(VerificationException.ErrorType.NOT_VERIFIED, "인증되지 않은 이메일입니다. 이메일: "+ request.getEmail());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.getPassword());

        //User 엔티티 생성 후 DB 저장
        User user = User.builder()
                .username(request.getUsername())
                .password(request.getPassword())
                .email(request.getEmail())
                .nickname(request.getNickname())
                .address(request.getAddress())
                .longitude(request.getLongitude())
                .latitude(request.getLatitude())
                .phoneNumber(request.getPhoneNumber())
                .profileImage(null)
                .build();

        userRepository.save(user);
    }
    
    @Transactional(readOnly = true)
    public User findUserById(Long userId) {
      return userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
    }

    @Transactional
    public User updateUserCredit(Long userId, int credit) {
      User user = userRepository.findById(userId)
        .orElseThrow(() -> new RuntimeException("User not found"));
      user.updateCredit(credit); // updateCredit() 메서드 호출
      return user;
    }
}
