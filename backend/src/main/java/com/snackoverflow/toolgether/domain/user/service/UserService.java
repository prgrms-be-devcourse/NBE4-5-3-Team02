package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.dto.request.SignupRequest;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.custom.duplicate.DuplicateFieldException;
import com.snackoverflow.toolgether.global.exception.custom.mail.VerificationException;
import com.snackoverflow.toolgether.global.exception.custom.user.UserNotFoundException;
import com.snackoverflow.toolgether.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationService verificationService;
    private final JwtUtil jwtUtil;

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
    @Transactional
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

    // 기본 사용자 로그인
    public LoginResult loginUser(String username, String password) {
        User user = userRepository.findByUsername(username).orElseThrow(() ->
                new UserNotFoundException("존재하지 않는 사용자: " + username));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new UserNotFoundException("비밀번호가 올바르지 않습니다.");
        }

        // username 기반으로 토큰 생성
        Map<String, Object> claims = new HashMap<>();
        claims.put("username", user.getUsername());
        String token = jwtUtil.createToken(claims);

        return new LoginResult(username, token);
    }

    public record LoginResult(String userName, String token) {}

    // 소셜 로그인

    // username 으로 사용자 찾기
    public User getUserForUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("가입되지 않은 유저입니다."));
    }

}
