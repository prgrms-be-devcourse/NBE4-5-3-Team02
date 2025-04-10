package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.request.PatchMyInfoRequest;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import com.snackoverflow.toolgether.global.util.s3.S3Service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final S3Service s3Service;

    public String checkMyInfoDuplicates(User user, PatchMyInfoRequest request) {
        User existingUserByNickname = userRepository.findByNickname(request.getNickname());
        if (existingUserByNickname != null && !existingUserByNickname.getId().equals(user.getId())) {
            return "닉네임";
        }
        return "";
    }

    public Optional<User> findByUserId(Long userId) {
        return userRepository.findById(userId);
    }

    public User findUserById(Long userId) {
        return userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
    }

    @Transactional
    public void updateUserCredit(Long userId, int credit) {
        User user = userRepository.findById(userId).orElseThrow(UserNotFoundException::new);
        user.updateCredit(credit); // updateCredit() 메서드 호출
    }

    @Transactional(readOnly = true)
    public com.snackoverflow.toolgether.domain.user.dto.response.MeInfoResponse getMeInfo(long id) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        return com.snackoverflow.toolgether.domain.user.dto.response.MeInfoResponse.from(user);
    }

    //프로필 이미지 업로드
    @Transactional
    public void postProfileImage(User user, MultipartFile profileImageFile) {
        deleteProfileImage(user);
        //S3Service 의 upload 메소드 호출, "profile" 폴더에 저장
        String profileImageUrl = s3Service.upload(profileImageFile, "profile");
        //S3 URL로 프로필 이미지 정보 업데이트
        user.updateProfileImage(profileImageUrl);
    }

    //프로필 이미지 삭제
    @Transactional
    public void deleteProfileImage(User user) {
        String profileImage = user.getProfileImage();

        if (profileImage != null) {
            s3Service.delete(profileImage);
            user.deleteProfileImage();
        }
    }

    @Transactional
    public void updateMyInfo(User user, PatchMyInfoRequest request) {
        user.updatePhoneNumber(request.getPhoneNumber());
        user.updateNickname(request.getNickname());
    }

    @Transactional
    public void deleteUser(User user) {
        user.delete();
    }

    @Transactional
    public void updateUserScore(Long id, double updatedScore) {
        User user = userRepository.findById(id).orElseThrow(UserNotFoundException::new);
        user.updateScore(updatedScore);
    }

    public List<User> getUsersWithoutReviewsSince(LocalDateTime date) {
        return userRepository.findUsersWithoutReviewsSince(date);
    }
}
