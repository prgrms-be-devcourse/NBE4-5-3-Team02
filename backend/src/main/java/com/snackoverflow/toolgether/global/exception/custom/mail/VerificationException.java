package com.snackoverflow.toolgether.global.exception.custom.mail;

import lombok.Getter;

@Getter
public class VerificationException extends RuntimeException {

    private final ErrorType errorType;

    public VerificationException(ErrorType errorType, String message) {
        super(message);
        this.errorType = errorType;
    }

    public enum ErrorType {
        REQUEST_NOT_FOUND, // 요청을 찾을 수 없음
        CODE_MISMATCH, // 코드가 일치하지 않음
        EXPIRED, // 만료된 인증 정보
        NOT_VERIFIED // 이메일이 인증되지 않음
    }
}
