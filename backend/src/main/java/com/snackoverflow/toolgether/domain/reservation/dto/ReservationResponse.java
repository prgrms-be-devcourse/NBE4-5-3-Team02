package com.snackoverflow.toolgether.domain.reservation.dto;

public record ReservationResponse(
	Long id,
	String status,
	Double amount
) {}
