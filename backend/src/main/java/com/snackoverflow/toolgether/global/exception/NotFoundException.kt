package com.snackoverflow.toolgether.global.exception

class NotFoundException(private val code: String, message: String?) : RuntimeException(message)

