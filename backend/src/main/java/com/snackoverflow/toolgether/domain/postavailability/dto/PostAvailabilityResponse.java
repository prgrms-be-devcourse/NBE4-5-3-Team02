package com.snackoverflow.toolgether.domain.postavailability.dto;

import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class PostAvailabilityResponse {

    private LocalDateTime startTime; // 거래 가능 시작 시간
    private LocalDateTime endTime; // 거래 가능 종료 시간
    private LocalDateTime date; // 특정 날짜 (반복이 아닌 경우)
    private Integer recurrenceDays; // 반복 요일 (월 - 1, 화 - 2, ..., 일 - 7)
    private boolean isRecurring; // 매주 반복 여부

    public PostAvailabilityResponse(PostAvailability availability) {
        this.startTime = availability.getStartTime();
        this.endTime = availability.getEndTime();
        this.date = availability.getDate();
        this.recurrenceDays = availability.getRecurrence_days();
        this.isRecurring = availability.isRecurring();
    }
}
