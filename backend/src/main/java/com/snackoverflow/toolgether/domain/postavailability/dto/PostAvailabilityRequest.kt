package com.snackoverflow.toolgether.domain.postavailability.dto

import jakarta.validation.constraints.NotNull
import lombok.Builder
import lombok.Getter
import java.time.LocalDateTime

@Getter
@Builder
class PostAvailabilityRequest {
    private val startTime: @NotNull(message = "거래 가능한 시작 시간을 입력해야 합니다.") LocalDateTime? = null // 거래 가능 시작 시간

    private val endTime: @NotNull(message = "거래 가능한 종료 시간을 입력해야 합니다.") LocalDateTime? = null // 거래 가능 종료 시간

    private val date: LocalDateTime? = null // 특정 날짜 (반복이 아닌 경우)

    private val recurrenceDays: Int? = null // 반복 요일 (월 - 1, 화 - 2, ..., 일 - 7)

    private val isRecurring = false // 매주 반복 여부
}
