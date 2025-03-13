package com.snackoverflow.toolgether.domain.postavailability.dto;

import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
public class PostAvailabilityResponse {
    private String date;
    private String startTime;
    private String endTime;
    private boolean isRecurring;
    private Integer recurrenceDays;

    public PostAvailabilityResponse(PostAvailability postAvailability, DateTimeFormatter formatter) {
        this.date = postAvailability.getDate() != null ? postAvailability.getDate().format(formatter) : null;
        this.startTime = postAvailability.getStartTime() != null ? postAvailability.getStartTime().format(formatter) : null;
        this.endTime = postAvailability.getEndTime() != null ? postAvailability.getEndTime().format(formatter) : null;
        this.isRecurring = postAvailability.isRecurring();
        this.recurrenceDays = postAvailability.getRecurrence_days();
    }
}
