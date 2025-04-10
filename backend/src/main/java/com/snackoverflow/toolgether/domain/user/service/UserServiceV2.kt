package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.v2.SignupRequestV2
import com.snackoverflow.toolgether.domain.user.entity.User
import com.snackoverflow.toolgether.domain.user.repository.UserRepository
import com.snackoverflow.toolgether.global.exception.ErrorCode
import com.snackoverflow.toolgether.global.exception.ServiceException
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional


/**
 * 휴대폰 인증, 좌표 -> 주소 변환, 추가 정보를 번호만 받도록 변경
 */
@Service
@Transactional(readOnly = true)
class UserServiceV2(
    private val userRepository: UserRepository,
    private val passwordEncoder: PasswordEncoder,
    private val locationService: LocationService
) {


    // 이메일 중복 방지 -> 실시간으로 중복 확인을 하기 위해서 메서드 분리
    fun checkEmailDuplicate(email: String) {
        if (userRepository.existsByEmail(email)) {
            throw ServiceException(ErrorCode.DUPLICATE_FIELD)
        }
    }

    // 닉네임 중복 방지
    fun checkNicknameDuplicate(nickname: String) {
        if (userRepository.existsByNickname(nickname)) {
            throw ServiceException(ErrorCode.DUPLICATE_FIELD)
        }
    }

    // 비밀번호 확인 필드
    fun checkPassword(password: String, confirmPassword: String) {
        if (!password.equals(confirmPassword)) {
            throw ServiceException(ErrorCode.PASSWORD_MISMATCH)
        }
    }

    // 일반 회원 가입 (역지오코딩)
    @Transactional
    fun registerUser(request: SignupRequestV2): Long {
        // 비밀번호 암호화
        val encodedPassword = passwordEncoder.encode(request.password)

        // 위도, 경도를 받아서 주소로 변환하는 로직 추가
        val address = locationService.convertCoordinateToAddress(request.latitude, request.longitude)

        // User 객체 생성
        val user = User.createGeneralUser(
            email = request.email,
            password = encodedPassword,
            phoneNumber = request.phoneNumber,
            nickname = request.nickname,
            baseAddress = address
        )

        userRepository.save(user)

        return user.id!!
    }

    // 사용자 로그인 -> 이메일, 패스워드가 맞는지 검증
    @Transactional
    fun authenticateUser(email: String, password: String) {
        if (!passwordEncoder.matches(password, getUserByEmail(email)?.password)) {
            throw ServiceException(ErrorCode.PASSWORD_MISMATCH)
        }
    }

    // 이전의 비밀번호와 변경하려는 비밀번호가 일치하는지 검증
    fun checkBeforePassword(id: Long, newPassword: String) {
        val user = userRepository.findByIdOrNull(id)
        if (passwordEncoder.matches(newPassword, user!!.password)) {
            throw ServiceException(ErrorCode.SAME_PASSWORD)
        }
    }

    @Transactional
    // 비밀번호 변경하기
    fun changePassword(id: Long, password: String) {
        val user = getUserById(id)
        val updatePassword = passwordEncoder.encode(password)
        user!!.updatePassword(updatePassword)
    }

    // 휴대폰 번호로 유저 이메일 찾기
    fun getUserEmail(phoneNumber: String): String {
        return ((userRepository.findByphoneNumber(phoneNumber)
            ?: throw UserNotFoundException())).email.toString()
    }

    // 프로필 가져오기
    fun getMyProfile(userId: Long): String {
        val user = getUserById(userId)
        return user?.profileImage.toString()
    }

    // 이메일로 유저 조회
    fun getUserByEmail(email: String): User {
        return userRepository.findByEmail(email) ?: throw UserNotFoundException()
    }

    // ID로 유저 조회
    fun getUserById(userId: Long): User? {
        return userRepository.findByIdOrNull(userId)
    }

    // 유저 존재 여부 확인
    fun existsUser(email: String): Boolean {
        return userRepository.existsByEmail(email)
    }
}

