package com.snackoverflow.toolgether.global.exception.custom.location;

// 공통 상위 클래스
public class LocationException extends RuntimeException {
    public LocationException(String message) {
        super(message);
    }

    public LocationException(String message, Throwable cause) {
        super(message, cause);
    }
}
