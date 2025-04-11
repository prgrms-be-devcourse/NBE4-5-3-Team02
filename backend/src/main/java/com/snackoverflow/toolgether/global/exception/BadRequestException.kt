package com.snackoverflow.toolgether.global.exception;

import lombok.Getter;

@Getter
public class BadRequestException extends RuntimeException{
    private final String code;

    public BadRequestException(String code, String message) {
        super(message);
        this.code = code;
    }

}

