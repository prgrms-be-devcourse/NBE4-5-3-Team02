package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.request.PatchMyInfoRequest;
import com.snackoverflow.toolgether.domain.user.dto.response.MeInfoResponse;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import com.snackoverflow.toolgether.global.util.s3.S3Service;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 로직 변경한 부분은 새롭게 클래스를 나누어서 작성
 */
@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository,
    private val s3Service: S3Service
) {

    fun checkMyInfoDuplicates(user: User, request: PatchMyInfoRequest): String {
        val existingUserByNickname = userRepository.findByNickname(request.nickname)
        return if (existingUserByNickname != null && existingUserByNickname.id != user.id) {
            "닉네임"
        } else {
            ""
        }
    }

    fun findByUserId(userId: Long): User? {
        return userRepository.findById(userId).orElse(null)
    }

    fun findUserById(userId: Long): User {
        return userRepository.findById(userId).orElseThrow { UserNotFoundException() }
    }

    @Transactional
    fun updateUserCredit(userId: Long, credit: Int) {
        val user = userRepository.findById(userId).orElseThrow { UserNotFoundException() }
        user.updateCredit(credit)
    }

    @Transactional(readOnly = true)
    fun getMeInfo(id: Long): MeInfoResponse {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException() }
        return MeInfoResponse.from(user)
    }

    @Transactional
    fun postProfileImage(user: User, profileImageFile: MultipartFile?) {
        deleteProfileImage(user)
        val profileImageUrl = s3Service.upload(profileImageFile, "profile") // S3에 업로드
        user.updateProfileImage(profileImageUrl) // 프로필 이미지 URL 업데이트
    }

    @Transactional
    fun deleteProfileImage(user: User) {
        user.profileImage?.let { profileImage ->
            s3Service.delete(profileImage) // S3에서 이미지 삭제
            user.deleteProfileImage() // 사용자 프로필 이미지 정보 삭제
        }
    }

    @Transactional
    fun updateMyInfo(user: User, request: PatchMyInfoRequest) {
        user.updatePhoneNumber(request.phoneNumber)
        user.updateNickname(request.nickname)
    }

    @Transactional
    fun deleteUser(user: User) {
        user.delete()
    }

    @Transactional
    fun updateUserScore(id: Long, updatedScore: Double) {
        val user = userRepository.findById(id).orElseThrow { UserNotFoundException() }
        user.updateScore(updatedScore)
    }

    fun getUsersWithoutReviewsSince(date: LocalDateTime): List<User> {
        return userRepository.findUsersWithoutReviewsSince(date)
    }
}
