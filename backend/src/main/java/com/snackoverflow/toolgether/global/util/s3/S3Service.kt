package com.snackoverflow.toolgether.global.util.s3

import org.springframework.web.multipart.MultipartFile

interface S3Service {
    fun upload(multipartFile: MultipartFile?, dirName: String): String
    fun delete(imageUrl: String)
}