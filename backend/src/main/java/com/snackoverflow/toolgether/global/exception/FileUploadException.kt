package com.snackoverflow.toolgether.global.exception;

import lombok.Getter;

@Getter
public class FileUploadException extends RuntimeException { // RuntimeException 직접 상속
    private final String code;

    public FileUploadException(String code, String message) {
        super(message);
        this.code = code;
    }
}