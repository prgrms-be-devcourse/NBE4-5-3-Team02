package com.snackoverflow.toolgether.domain.review.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class ReviewRequest {
    @NotNull
    private Long reservationId;

    @Min(value = 1)
    @Max(value = 5)
    private int productScore;

    @Min(value = 1)
    @Max(value = 5)
    private int timeScore;

    @Min(value = 1)
    @Max(value = 5)
    private int kindnessScore;
}
