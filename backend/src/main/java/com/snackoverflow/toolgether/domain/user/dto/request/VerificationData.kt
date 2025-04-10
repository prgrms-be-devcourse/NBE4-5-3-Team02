package com.snackoverflow.toolgether.domain.user.dto.request;

import java.time.LocalDateTime;

/**
 * TODO 변환 이후 @JvmOverloads constructor 제거할 것
 */
data class VerificationData @JvmOverloads constructor (
    val email: String,
    val code: String,
    var verified: Boolean,
    val createdAt: LocalDateTime = LocalDateTime.now(),
    val expirationMinutes: Int = 15 // 15분 고정
) {
    var attemptCount: Int = 0
        private set

    // 시도 횟수 증가
    fun incrementAttempt(): Int {
        return ++attemptCount
    }

    // 인증 만료 여부 체크
    fun isExpired(): Boolean {
        return LocalDateTime.now().isAfter(createdAt.plusMinutes(expirationMinutes.toLong()))
    }
}