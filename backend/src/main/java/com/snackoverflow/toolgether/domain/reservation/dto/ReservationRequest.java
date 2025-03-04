package com.snackoverflow.toolgether.domain.reservation.dto;

import java.time.LocalDateTime;

public record ReservationRequest(
	Long postId,
	Long renterId,
	Long ownerId,
	LocalDateTime startTime,
	LocalDateTime endTime,
	Double deposit,
	Double rentalFee
) {}
