package com.snackoverflow.toolgether.global.exception.handler;

import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.exception.custom.CustomException;
import com.snackoverflow.toolgether.global.exception.custom.UserNotFoundException;
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import javax.naming.AuthenticationException

@RestControllerAdvice
class GlobalExceptionHandler {

    companion object {
        private val log = LoggerFactory.getLogger(GlobalExceptionHandler::class.java)
    }

    data class ErrorResponse(
        val code: String,
        val message: Any
    ) {
        companion object {
            fun of(code: String, message: Any): ErrorResponse {
                return ErrorResponse(code, message)
            }
        }
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgument(exception: IllegalArgumentException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(400)
            .body(ErrorResponse.of("INVALID_ARGUMENT", exception.message ?: "Invalid argument"))
    }

    @ExceptionHandler(UserNotFoundException::class)
    fun handleUserNotFound(e: UserNotFoundException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(404).body(ErrorResponse.of("USER_NOT_FOUND", "사용자를 찾을 수 없습니다."))
    }

    @ExceptionHandler(CustomException::class)
    fun handleCustomException(e: CustomException): ResponseEntity<com.snackoverflow.toolgether.global.exception.custom.ErrorResponse> {
        return ResponseEntity
            .status(e.errorResponse.status)
            .body(e.errorResponse)
    }

    @ExceptionHandler(AuthenticationException::class)
    fun handleAuthenticationException(e: AuthenticationException): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(401).body(ErrorResponse.of("401", e.message ?: "Authentication credentials not found"))
    }

    @ExceptionHandler(ServiceException::class)
    fun handleServiceException(ex: ServiceException): ResponseEntity<ErrorResponse> {
        if (ex.cause != null) {
            log.error("Service Exception 발생: code={}, message={}, cause={}", ex.code, ex.message, ex.cause?.message)
        } else {
            log.error("Service Exception 발생: code={}, message={}", ex.code, ex.message)
        }
        return ResponseEntity.status(ex.status).body(ErrorResponse.of(ex.code, ex.message))
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValid(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        val errors = HashMap<String, String?>()
        e.bindingResult.fieldErrors.forEach { error ->
            errors[error.field] = error.defaultMessage
        }
        return ResponseEntity.status(400).body(ErrorResponse.of("METHOD_ARGUMENT_NOT_VALID", errors))
    }
}