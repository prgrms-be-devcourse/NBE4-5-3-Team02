package com.snackoverflow.toolgether.global.util.s3

import com.amazonaws.services.s3.AmazonS3Client
import com.snackoverflow.toolgether.domain.postimage.repository.PostImageRepository
import com.snackoverflow.toolgether.domain.user.repository.UserRepository
import org.slf4j.LoggerFactory // LoggerFactory import 추가
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import java.util.stream.Collectors
import java.util.stream.Stream

@Service
class S3OrphanedImageCleanupService(
    private val amazonS3Client: AmazonS3Client,
    private val postImageRepository: PostImageRepository,
    private val userRepository: UserRepository,
    private val s3Service: S3Service,
    @Value("\${cloud.aws.s3.bucket}")
    private val bucketName: String
) {

    private val log = LoggerFactory.getLogger(S3OrphanedImageCleanupService::class.java) // 로거 직접 생성

    @Scheduled(cron = "0 0 3 1 1,4,7,10 *")
    fun cleanupOrphanedImages() {
        log.info("S3 버킷 orphaned 이미지 삭제 스케줄러 시작")

        val dbPostImageUrls = postImageRepository.findAllImageUrl()

        val dbProfileImageUrls = userRepository.findAllProfileImageUrl()

        val allDbImageUrls = Stream.concat(dbPostImageUrls.stream(), dbProfileImageUrls.stream())
            .collect(Collectors.toList())

        val result = amazonS3Client.listObjectsV2(bucketName)
        val objects = result.objectSummaries

        var deletedCount = 0

        for (s3Object in objects) {
            val s3ObjectKey = s3Object.key
            val s3ImageUrl = amazonS3Client.getUrl(bucketName, s3ObjectKey).toString()

            if (!allDbImageUrls.contains(s3ImageUrl)) {
                log.info("사용되지 않는 S3 버킷 내 이미지 URL: {}", s3ImageUrl)
                s3Service.delete(s3ImageUrl)
                deletedCount++
            }
        }

        log.info("S3 버킷에서 사용되지 않는 이미지 삭제 스케줄러 종료. 총 {}개의 이미지 삭제됨", deletedCount)
    }
}