package com.snackoverflow.toolgether.global.exception

import org.springframework.http.HttpStatus

/**
 * TODO 변환 이후 @JvmOverloads constructor 제거할 것
 */

class ServiceException @JvmOverloads constructor (
    val errorCode: ErrorCode, // ErrorCode 객체를 저장
    cause: Throwable? = null // 기본값을 null로 설정하여 선택적으로 예외를 전달 (상위 예외가 있을 경우)
) : RuntimeException(errorCode.message, cause) {

    val code: String
        get() = errorCode.code

    override val message: String
        get() = errorCode.message

    val status: HttpStatus
        get() = errorCode.status
}