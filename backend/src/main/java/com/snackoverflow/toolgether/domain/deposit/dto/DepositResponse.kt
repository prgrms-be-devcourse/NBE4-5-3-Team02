package com.snackoverflow.toolgether.domain.deposit.dto;

public record DepositResponse(
	Long id,
	String status,
	Long reservationId,
	String returnReason,
	int amount
) {}
