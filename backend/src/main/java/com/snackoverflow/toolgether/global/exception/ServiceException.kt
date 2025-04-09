package com.snackoverflow.toolgether.global.exception

import com.snackoverflow.toolgether.global.dto.RsData


class ServiceException(code: String, message: String) : RuntimeException(message) {
    private val rsData: RsData<*> = RsData<Any>(code, message)

    val code: String
        get() = rsData.code

    val msg: String
        get() = rsData.msg

    val statusCode: Int
        get() = rsData.statusCode
}