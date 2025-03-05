package com.snackoverflow.toolgether.global.exception;

import lombok.Builder;
import lombok.Getter;

import java.net.URI;

@Getter
@Builder
public class ErrorResponse {

	private String title;
	private Integer status;
	private String detail;
	private URI instance;
}
