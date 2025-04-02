package com.snackoverflow.toolgether.global.util.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.ListObjectsV2Result;
import com.amazonaws.services.s3.model.S3ObjectSummary;
import com.snackoverflow.toolgether.domain.postimage.repository.PostImageRepository;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3OrphanedImageCleanupService {

    private final AmazonS3Client amazonS3Client;
    private final PostImageRepository postImageRepository;
    private final UserRepository userRepository;
    private final S3Service s3Service;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    @Scheduled(cron = "0 0 3 1 1,4,7,10 *")
    public void cleanupOrphanedImages() {
        log.info("S3 버킷 orphaned 이미지 삭제 스케줄러 시작");

        List<String> dbPostImageUrls = postImageRepository.findAllImageUrl();

        List<String> dbProfileImageUrls = userRepository.findAllProfileImageUrl();

        List<String> allDbImageUrls = Stream.concat(dbPostImageUrls.stream(), dbProfileImageUrls.stream())
                .collect(Collectors.toList());

        ListObjectsV2Result result = amazonS3Client.listObjectsV2(bucketName);
        List<S3ObjectSummary> objects = result.getObjectSummaries();

        int deletedCount = 0;

        for (S3ObjectSummary object : objects) {
            String s3ObjectKey = object.getKey();
            String s3ImageUrl = amazonS3Client.getUrl(bucketName, s3ObjectKey).toString();

            if (!allDbImageUrls.contains(s3ImageUrl)) {
                log.info("사용되지 않는 S3 버킷 내 이미지 URL: {}", s3ImageUrl);
                s3Service.delete(s3ImageUrl);
                deletedCount++;
            }
        }

        log.info("S3 버킷에서 사용되지 않는 이미지 삭제 스케줄러 종료. 총 {}개의 이미지 삭제됨", deletedCount);
    }
}