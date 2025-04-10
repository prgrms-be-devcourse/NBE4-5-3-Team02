package com.snackoverflow.toolgether.global.exception.custom;

class CustomException(
		val errorResponse: ErrorResponse
) : RuntimeException(errorResponse.detail)