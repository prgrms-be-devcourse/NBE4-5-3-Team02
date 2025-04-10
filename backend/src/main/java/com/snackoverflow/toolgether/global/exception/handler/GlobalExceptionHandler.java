package com.snackoverflow.toolgether.global.exception.handler;

import com.snackoverflow.toolgether.global.exception.custom.CustomException;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import com.snackoverflow.toolgether.global.exception.custom.duplicate.DuplicateFieldException;
import com.snackoverflow.toolgether.global.exception.custom.location.AddressConversionException;
import com.snackoverflow.toolgether.global.exception.custom.location.DistanceCalculationException;
import com.snackoverflow.toolgether.global.exception.custom.location.LocationException;
import com.snackoverflow.toolgether.global.exception.custom.mail.MailPreparationException;
import com.snackoverflow.toolgether.global.exception.custom.mail.SmtpConnectionException;
import com.snackoverflow.toolgether.global.exception.custom.mail.VerificationException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @Getter
    @Builder
    @AllArgsConstructor
    static class ErrorResponse {
        private String code;
        private String message;
        private LocalDateTime time;

        public static ErrorResponse of(String code, String message) {
            return ErrorResponse.builder()
                    .code(code)
                    .message(message)
                    .time(LocalDateTime.now())
                    .build();
        }
    }

    @ExceptionHandler(MailPreparationException.class)
    public ResponseEntity<ErrorResponse> handleMailPrep(MailPreparationException exception) {
        return ResponseEntity.status(400)
                .body(ErrorResponse.of("MAIL 400-1", exception.getMessage()));
    }

    @ExceptionHandler(SmtpConnectionException.class)
    public ResponseEntity<ErrorResponse> handleMailPrep(SmtpConnectionException exception) {
        return ResponseEntity.status(500)
                .body(ErrorResponse.of("MAIL 500-1", exception.getMessage()));
    }

    @ExceptionHandler(VerificationException.class)
    public ResponseEntity<ErrorResponse> handleVerification(VerificationException exception) {
        return switch (exception.getErrorType()) {
            case REQUEST_NOT_FOUND ->
                    ResponseEntity.status(404).body(ErrorResponse.of("EMAIL-AUTH-404", exception.getMessage()));
            case CODE_MISMATCH ->
                    ResponseEntity.status(409).body(ErrorResponse.of("EMAIL-AUTH-409", exception.getMessage()));
            case EXPIRED -> ResponseEntity.status(410).body(ErrorResponse.of("EMAIL-AUTH-410", exception.getMessage()));
            case NOT_VERIFIED ->
                    ResponseEntity.status(400).body(ErrorResponse.of("EMAIL-AUTH-400", exception.getMessage()));
        };
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException exception) {
        return ResponseEntity.status(400).body(ErrorResponse.of("INVALID_ARGUMENT", exception.getMessage()));
    }

    @ExceptionHandler(DuplicateFieldException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateField(DuplicateFieldException exception) {
        return ResponseEntity.status(409).body(ErrorResponse.of(
                "FIELD-CONFLICT-409", exception.getMessage()));
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity.status(404).body(ErrorResponse.of("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."));
    }

    @ExceptionHandler(LocationException.class)
    public ResponseEntity<ErrorResponse> handleLocationException(LocationException exception) {
        String errorCode = exception instanceof AddressConversionException ? "ADDRESS_CONVERSION_ERROR"
                : exception instanceof DistanceCalculationException ? "DISTANCE_CALCULATION_ERROR"
                : "LOCATION_ERROR";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                ErrorResponse.of(errorCode, exception.getMessage()));
    }

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<com.snackoverflow.toolgether.global.exception.custom.ErrorResponse> handleCustomException(CustomException ex) {
        return ResponseEntity
                .status(ex.getErrorResponse().getStatus())
                .body(ex.getErrorResponse());
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationCredentialsNotFoundException exception) {
        return ResponseEntity.status(401).body(ErrorResponse.of("401", exception.getMessage()));
    }
}
