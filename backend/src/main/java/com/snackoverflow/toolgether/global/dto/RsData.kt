package com.snackoverflow.toolgether.global.dto

import com.fasterxml.jackson.annotation.JsonIgnore

data class RsData<T>(
    val code: String,
    val msg: String,
    val data: T? = null
) {
    val statusCode: Int
        @JsonIgnore
        get() = code.split("-")[0].toInt()

    val isSuccess: Boolean
        @JsonIgnore
        get() = code.startsWith("2")

    constructor(code: String, msg: String) : this(code, msg, null)
}