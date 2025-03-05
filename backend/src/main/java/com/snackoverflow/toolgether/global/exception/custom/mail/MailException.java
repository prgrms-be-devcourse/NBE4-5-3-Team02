package com.snackoverflow.toolgether.global.exception.custom.mail;

// 메일 예외 베이스 클래스
public abstract class MailException extends RuntimeException {
    public MailException(String message, Throwable cause) {
        super(message, cause);
    }
}
