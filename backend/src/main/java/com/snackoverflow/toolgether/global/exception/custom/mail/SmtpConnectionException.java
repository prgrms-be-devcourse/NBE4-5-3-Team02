package com.snackoverflow.toolgether.global.exception.custom.mail;

// SMTP 연결 오류
public class SmtpConnectionException extends MailException {
    public SmtpConnectionException(String message, Throwable cause) {
        super(message, cause);
    }
}
