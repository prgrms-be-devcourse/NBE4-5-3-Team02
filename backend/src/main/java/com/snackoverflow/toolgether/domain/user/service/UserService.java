package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.request.PatchMyInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.Address;
import com.snackoverflow.toolgether.global.util.s3.S3Service;
import org.springframework.transaction.annotation.Transactional;
import com.snackoverflow.toolgether.domain.user.dto.MeInfoResponse;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.dto.request.SignupRequest;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.custom.duplicate.DuplicateFieldException;
import com.snackoverflow.toolgether.global.exception.custom.mail.VerificationException;
import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.exception.custom.user.UserNotFoundException;
import com.snackoverflow.toolgether.global.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    private final S3Service s3Service;

    // 이메일, 아이디, 닉네임 중복 방지
    public void checkDuplicates(SignupRequest request) {
        if (userRepository.existsByUsername(request.username())) {
            throw new DuplicateFieldException("사용자 ID 중복 오류 발생");
        }
        if (userRepository.existsByEmail(request.email())) {
            throw new DuplicateFieldException("사용자 EMAIL 중복 오류 발생");
        }
        if (userRepository.existsByNickname(request.nickname())) {
            throw new DuplicateFieldException("사용자 닉네임 중복 오류 발생");
        }
    }

    public void checkMyInfoDuplicates(PatchMyInfoRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateFieldException("사용자 EMAIL 중복 오류 발생");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateFieldException("사용자 닉네임 중복 오류 발생");
        }
        if (userRepository.existsByPhoneNumber(request.getPhoneNumber())) {
            throw new DuplicateFieldException("사용자 전화번호 중복 오류 발생");
        }
    }

    // 회원 가입
    @Transactional
    public void registerVerifiedUser(SignupRequest request) {
        // 이메일 인증 완료 시 회원 가입 허용
        if (!verificationService.isEmailVerified(request.email())) {
            throw new VerificationException(VerificationException.ErrorType.NOT_VERIFIED, "인증되지 않은 이메일입니다. 이메일: "+ request.email());
        }

        // 비밀번호 암호화
        String encodedPassword = passwordEncoder.encode(request.password());

        // 위치 인증 후 회원 가입 가능


        //User 엔티티 생성 후 DB 저장
        User user = User.builder()
                .username(request.username())
                .password(request.password())
                .email(request.email())
                .nickname(request.nickname())
                .address(Address.builder()
                        .zipcode(request.postalCode())
                        .mainAddress(request.baseAddress())
                        .detailAddress(request.detailAddress())
                        .build())
                .longitude(request.longitude())
                .latitude(request.latitude())
                .phoneNumber(request.phoneNumber())
                .profileImage(null)
                .additionalInfoRequired(false)
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

    // username 으로 사용자 찾기
    public User getUserForUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() -> new UserNotFoundException("가입되지 않은 유저입니다."));
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

    @Transactional(readOnly = true)
    public MeInfoResponse getMeInfo(long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new ServiceException("404-1", "해당 유저를 찾을 수 없습니다")
        );
        return MeInfoResponse.from(user);
    }

    public User findByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(
                () -> new ServiceException("404-1", "해당 유저를 찾을 수 없습니다")
        );
    }

    //프로필 이미지 업로드
    @Transactional
    public void postProfileImage(User user, MultipartFile profileImageFile) {
        deleteProfileImage(user);
        //S3Service 의 upload 메소드 호출, "profile" 폴더에 저장
        String profileImageUrl = s3Service.upload(profileImageFile, "profile");
        //S3 URL로 프로필 이미지 정보 업데이트
        user.updateProfileImage(profileImageUrl);
        userRepository.save(user);
    }

    //프로필 이미지 삭제
    @Transactional
    public void deleteProfileImage(User user) {
        String profileImage = user.getProfileImage();

        if (profileImage != null) {
            s3Service.delete(profileImage);
            user.deleteProfileImage();
            userRepository.save(user);
        }
    }

    @Transactional
    public void updateMyInfo(User user, PatchMyInfoRequest request) {
        user.updateEmail(request.getEmail());
        user.updatePhoneNumber(request.getPhoneNumber());
        user.updateNickname(request.getNickname());
        user.updateAddress(request.getAddress().getMainAddress(), request.getAddress().getDetailAddress(), request.getAddress().getZipcode());
        user.updateLocation(request.getLatitude(), request.getLongitude());
        userRepository.save(user);
    }

    @Transactional
    public void deleteUser(User user) {
        user.delete();
        userRepository.save(user);
    }
}
