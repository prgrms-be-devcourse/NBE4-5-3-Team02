package com.snackoverflow.toolgether.global.exception

class FileUploadException(// RuntimeException 직접 상속
    private val code: String, message: String?
) : RuntimeException(message) 