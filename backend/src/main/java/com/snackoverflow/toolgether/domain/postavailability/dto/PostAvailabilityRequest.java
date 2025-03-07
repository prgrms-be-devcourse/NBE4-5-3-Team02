package com.snackoverflow.toolgether.domain.postavailability.dto;

import lombok.Builder;
import lombok.Getter;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;

@Getter
@Builder
public class PostAvailabilityRequest {

    @NotNull(message = "거래 가능한 시작 시간을 입력해야 합니다.")
    private LocalDateTime startTime; // 거래 가능 시작 시간

    @NotNull(message = "거래 가능한 종료 시간을 입력해야 합니다.")
    private LocalDateTime endTime; // 거래 가능 종료 시간

    private LocalDateTime date; // 특정 날짜 (반복이 아닌 경우)

    private Integer recurrenceDays; // 반복 요일 (월 - 1, 화 - 2, ..., 일 - 7)

    private boolean isRecurring; // 매주 반복 여부
}
