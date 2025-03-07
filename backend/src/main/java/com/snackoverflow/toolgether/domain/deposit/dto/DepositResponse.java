package com.snackoverflow.toolgether.domain.deposit.dto;

import java.time.LocalDateTime;

public record DepositResponse(
	Long id,
	String status,
	Long reservationId,
	String returnReason,
	int amount
) {}
