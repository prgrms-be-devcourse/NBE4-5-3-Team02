package com.snackoverflow.toolgether.domain.user.dto;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import lombok.NonNull;

import java.time.LocalDateTime;

public record MyReservationInfoResponse(
        @NonNull Long id,
        @NonNull String title,
        String image,
        @NonNull Double amount,
        @NonNull LocalDateTime startTime,
        @NonNull LocalDateTime endTime,
        @NonNull String status,
        @NonNull Boolean isReviewed
) {
    public static MyReservationInfoResponse from(Reservation reservation, String imageUrl, boolean isReviewed) {

        return new MyReservationInfoResponse(
                reservation.getId(),
                reservation.getPost().getTitle(),
                imageUrl,
                reservation.getAmount(),
                reservation.getStartTime(),
                reservation.getEndTime(),
                reservation.getStatus().toString(),
                isReviewed
        );
    }
}