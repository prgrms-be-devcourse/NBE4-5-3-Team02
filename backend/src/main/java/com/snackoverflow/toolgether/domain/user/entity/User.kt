package com.snackoverflow.toolgether.domain.user.entity;

import com.snackoverflow.toolgether.global.util.UUIDUtil
import jakarta.persistence.*
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime
import kotlin.math.roundToInt

@Entity
@EntityListeners(AuditingEntityListener::class)
@Table(name = "users") // 예약어 변경
class User(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    var password: String? = null, // 암호화된 비밀번호 (일반 사용자)

    @Column(unique = true)
    var email: String? = null, // 사용자 email

    var providerId: String? = null, // 소셜 로그인 사용자 ID

    var provider: String? = null, // 소셜 로그인 제공자

    @Column(unique = true)
    var phoneNumber: String? = null, // 전화번호

    @Column(unique = true)
    var nickname: String? = null, // 사용자 별명

    @CreatedDate
    var createdAt: LocalDateTime? = null, // 가입 일자

    var additionalInfoRequired: Boolean = false, // 추가 정보 필요 플래그

    var profileImage: String? = null, // 사용자 프로필 이미지 링크 저장

    var score: Int = 30, // 유저 평가 정보: 기본값 30점

    var credit: Int = 0, // 보증금 환불 필드

    var deletedAt: LocalDateTime? = null, // 탈퇴 일자, 탈퇴하면 null이 아님

    var baseAddress: String? = null // 기본 주소
) {
    companion object {
        // 일반 회원 가입
        @JvmStatic
        fun createGeneralUser(
            email: String,
            password: String,
            phoneNumber: String,
            nickname: String,
            baseAddress: String
        ) = User(
            email = email,
            password = password,
            phoneNumber = phoneNumber,
            nickname = nickname,
            baseAddress = baseAddress
        )

        // 소셜 로그인 회원 가입
        @JvmStatic
        fun createSocialUser(
            providerId: String,
            provider: String,
            phoneNumber: String,
            email: String,
            nickname: String,
            baseAddress: String
        ) = User(
            providerId = providerId,
            provider = provider,
            email = email,
            phoneNumber = phoneNumber,
            nickname = nickname,
            baseAddress = baseAddress,
            additionalInfoRequired = true
        )
    }

    // 업데이트 로직
    fun updateCredit(credit: Int) {
        this.credit += credit
    }

    fun updatePhoneNumber(phoneNumber: String) {
        this.phoneNumber = phoneNumber
    }

    fun updateAdditionalInfoRequired(additionalInfoRequired: Boolean) {
        this.additionalInfoRequired = additionalInfoRequired
    }

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    fun updateProfileImage(uuid: String) {
        this.profileImage = uuid
    }

    fun deleteProfileImage() {
        this.profileImage = null
    }

    // 탈퇴 시 호출, 삭제 시간을 기록하고 익명화 진행
    @PreUpdate
    fun preUpdate() {
        if (this.deletedAt != null) {
            anonymize()
        }
    }

    fun delete() {
        this.deletedAt = LocalDateTime.now()
    }

    // 개인 정보 익명화
    fun anonymize() {
        this.password = UUIDUtil.generateUUIDMasking()
        this.email = "${UUIDUtil.generateUUIDMasking()}@deleted.com"
        this.phoneNumber = UUIDUtil.generateUUIDMasking()
        this.nickname = "삭제된유저-${UUIDUtil.generateUUIDMasking()}"
        this.baseAddress = "삭제된 주소"
        this.profileImage = null
    }

    fun updateScore(updatedScore: Double) {
        this.score = updatedScore.roundToInt()
    }

    fun updateBaseAddress(baseAddress: String) {
        this.baseAddress = baseAddress
    }

    fun updatePassword(password: String) {
        this.password = password
    }
}
