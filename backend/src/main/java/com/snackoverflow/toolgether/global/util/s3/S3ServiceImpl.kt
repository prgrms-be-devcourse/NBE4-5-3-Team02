package com.snackoverflow.toolgether.global.util.s3

import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.model.DeleteObjectRequest
import com.amazonaws.services.s3.model.ObjectMetadata
import com.amazonaws.services.s3.model.PutObjectRequest
import com.snackoverflow.toolgether.global.exception.FileUploadException
import lombok.RequiredArgsConstructor
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import java.io.IOException
import java.util.*

// FileUploadException import 유지
@Service
@RequiredArgsConstructor
class S3ServiceImpl(
    private val amazonS3Client: AmazonS3Client,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucketName: String
) : S3Service {

    override fun upload(multipartFile: MultipartFile, dirName: String): String {
        val filename = "$dirName/${UUID.randomUUID()}"
        val objectMetadata = ObjectMetadata()
        objectMetadata.contentLength = multipartFile.size
        objectMetadata.contentType = multipartFile.contentType

        try {
            val request = PutObjectRequest(bucketName, filename, multipartFile.inputStream, objectMetadata)
            amazonS3Client.putObject(request)
        } catch (e: IOException) {
            throw FileUploadException("500", "파일 업로드에 실패했습니다.") // HttpStatus.INTERNAL_SERVER_ERROR 전달
        }

        return amazonS3Client.getUrl(bucketName, filename).toString()
    }

    override fun delete(imageUrl: String) {
        val filename = extractFilenameFromUrl(imageUrl)
        val deleteObjectRequest = DeleteObjectRequest(bucketName, filename)
        amazonS3Client.deleteObject(deleteObjectRequest)
    }

    private fun extractFilenameFromUrl(imageUrl: String): String {
        val parts = imageUrl.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        return parts[3] + "/" + parts[4]
    }
}