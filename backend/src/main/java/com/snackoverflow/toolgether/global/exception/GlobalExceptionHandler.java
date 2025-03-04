package com.snackoverflow.toolgether.global.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReservationException.class)
    public ResponseEntity<ErrorResponse> handleCustomException(ReservationException ex) {
        return ResponseEntity
            .status(ex.getErrorResponse().getStatus())
            .body(ex.getErrorResponse());
    }
}
