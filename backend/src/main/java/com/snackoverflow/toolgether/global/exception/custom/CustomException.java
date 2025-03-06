package com.snackoverflow.toolgether.global.exception.custom;

import lombok.Getter;

@Getter
public class CustomException extends RuntimeException {

	private final ErrorResponse errorResponse;

	public CustomException(ErrorResponse errorResponse) {
		super(errorResponse.getDetail());
		this.errorResponse = errorResponse;
	}
}