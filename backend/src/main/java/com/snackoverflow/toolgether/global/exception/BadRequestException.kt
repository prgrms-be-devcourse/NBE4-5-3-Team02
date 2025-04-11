package com.snackoverflow.toolgether.global.exception

class BadRequestException(private val code: String, message: String?) : RuntimeException(message)

