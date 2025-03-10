package com.snackoverflow.toolgether.domain.reservation.dto;

import java.time.LocalDateTime;

import com.snackoverflow.toolgether.domain.post.entity.Post;

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
