package com.snackoverflow.toolgether.global.exception.custom

import java.net.URI

class ErrorResponse(title: String, status: Int, detail: String, instance: URI) {
    lateinit var title: String
    var status: Int = 0
    lateinit var detail: String
    lateinit var instance: URI
}