package com.snackoverflow.toolgether.global.exception;

import lombok.Getter;

@Getter
public class ReservationException extends RuntimeException {

	private final ErrorResponse errorResponse;

	public ReservationException(ErrorResponse errorResponse) {
		super(errorResponse.getDetail());
		this.errorResponse = errorResponse;
	}
}