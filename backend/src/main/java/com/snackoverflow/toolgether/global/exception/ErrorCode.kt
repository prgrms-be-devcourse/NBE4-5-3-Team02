package com.snackoverflow.toolgether.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val code: String,
    val message: String,
    val status: HttpStatus
) {
    // Verification Errors
    REQUEST_NOT_FOUND("400-1", "요청을 찾을 수 없습니다.", HttpStatus.BAD_REQUEST),
    CODE_MISMATCH("400-2", "코드가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    EXPIRED("400-3", "만료된 인증 정보입니다.", HttpStatus.BAD_REQUEST),
    NOT_VERIFIED("400-4", "이메일이 인증되지 않았습니다.", HttpStatus.BAD_REQUEST),
    REQUEST_LIMIT_EXCEEDED("429-1", "인증 요청이 너무 많습니다.", HttpStatus.TOO_MANY_REQUESTS),

    // Auth Errors
    TOKEN_EXPIRED("401-1", "토큰이 만료되었습니다.", HttpStatus.UNAUTHORIZED),
    TOKEN_NOT_FOUND("404-1", "토큰을 갖고 있지 않습니다.", HttpStatus.NOT_FOUND),
    IN_BLACKLIST("403-2", "블랙리스트에 저장된 토큰입니다. 사용 불가합니다.", HttpStatus.FORBIDDEN),
    USERINFO_NOT_FOUND("401-2", "사용자 정보를 가져오지 못했습니다.", HttpStatus.UNAUTHORIZED),

    // Mail Errors
    MAIL_SEND_FAILED("500-1", "이메일 발송 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // User Errors
    SAME_PASSWORD("400-5", "동일한 비밀번호입니다.", HttpStatus.BAD_REQUEST),
    DUPLICATE_FIELD("409-1", "중복값이 존재합니다.", HttpStatus.CONFLICT),
    PASSWORD_MISMATCH("400-6", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),

    // Message Errors
    MESSAGE_SEND_FAILED("500-2", "인증 메시지 전송 중 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),

    // Location Errors
    ADDRESS_CONVERSION_ERROR("400-6", "좌표를 주소로 변환할 수 없습니다.", HttpStatus.BAD_REQUEST);

    fun toException(cause: Throwable? = null): ServiceException {
        return ServiceException(this, cause)
    }
}