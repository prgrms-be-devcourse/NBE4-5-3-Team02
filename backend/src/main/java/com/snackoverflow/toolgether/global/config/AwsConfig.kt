package com.snackoverflow.toolgether.global.config

import com.amazonaws.auth.AWSCredentials
import com.amazonaws.auth.AWSStaticCredentialsProvider
import com.amazonaws.auth.BasicAWSCredentials
import com.amazonaws.services.s3.AmazonS3Client
import com.amazonaws.services.s3.AmazonS3ClientBuilder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AwsConfig (
    @Value("\${cloud.aws.credentials.access-key}")
    private val accessKey: String,
    @Value("\${cloud.aws.credentials.secret-key}")
    private val secretKey: String,
    @Value("\${cloud.aws.region.static}")
    private val region: String
) {
    @Bean
    fun amazonS3Client(): AmazonS3Client {
        val credentials: AWSCredentials = BasicAWSCredentials(accessKey, secretKey) // AWS 자격 증명 생성
        return AmazonS3ClientBuilder.standard()
            .withCredentials(AWSStaticCredentialsProvider(credentials)) // 자격 증명 설정
            .withRegion(region) // 리전 설정
            .build() as AmazonS3Client // AmazonS3Client Bean 생성
    }
}