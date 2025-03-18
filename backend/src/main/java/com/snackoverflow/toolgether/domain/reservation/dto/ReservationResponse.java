package com.snackoverflow.toolgether.domain.reservation.dto;

import java.time.LocalDateTime;

public record ReservationResponse(
	Long id,
	String status,
	Long postId,
	LocalDateTime startTime,
	LocalDateTime endTime,
	Double amount,
	String rejectionReason,
	Long ownerId,
	Long renterId
) {}
