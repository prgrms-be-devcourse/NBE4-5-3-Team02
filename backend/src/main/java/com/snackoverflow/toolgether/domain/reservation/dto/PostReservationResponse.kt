package com.snackoverflow.toolgether.domain.reservation.dto;

public record PostReservationResponse(
	Long id,
	Long userId,
	String title,
	String priceType,
	Integer price
) {
}
