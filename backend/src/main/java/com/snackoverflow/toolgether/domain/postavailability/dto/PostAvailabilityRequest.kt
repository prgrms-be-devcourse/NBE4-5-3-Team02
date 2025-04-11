package com.snackoverflow.toolgether.domain.postavailability.dto

import jakarta.validation.constraints.NotNull
import java.time.LocalDateTime

data class PostAvailabilityRequest(
    @field:NotNull(message = "거래 가능한 시작 시간을 입력해야 합니다.") val startTime: LocalDateTime, // 거래 가능 시작 시간

    @field:NotNull(message = "거래 가능한 종료 시간을 입력해야 합니다.") val endTime: LocalDateTime, // 거래 가능 종료 시간

    val date: LocalDateTime? = null, // 특정 날짜 (반복이 아닌 경우)

    val recurrenceDays: Int = 0, // 반복 요일 (월 - 1, 화 - 2, ..., 일 - 7)

    val isRecurring: Boolean = false, // 매주 반복 여부
)