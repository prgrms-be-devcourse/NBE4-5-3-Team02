package com.snackoverflow.toolgether.global.util.s3;

import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.DeleteObjectRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.snackoverflow.toolgether.global.exception.FileUploadException; // FileUploadException import 유지
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class S3ServiceImpl implements S3Service {

    private final AmazonS3Client amazonS3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    public String upload(MultipartFile multipartFile, String dirName) {
        String filename = dirName + "/" + UUID.randomUUID();
        ObjectMetadata objectMetadata = new ObjectMetadata();
        objectMetadata.setContentLength(multipartFile.getSize());
        objectMetadata.setContentType(multipartFile.getContentType());

        try {
            PutObjectRequest request = new PutObjectRequest(bucketName, filename, multipartFile.getInputStream(), objectMetadata);
            amazonS3Client.putObject(request);
        } catch (IOException e) {
            throw new FileUploadException("500", "파일 업로드에 실패했습니다."); // HttpStatus.INTERNAL_SERVER_ERROR 전달
        }

        return amazonS3Client.getUrl(bucketName, filename).toString();
    }

    public void delete(String imageUrl) {
        String filename = extractFilenameFromUrl(imageUrl);
        DeleteObjectRequest deleteObjectRequest = new DeleteObjectRequest(bucketName, filename);
        amazonS3Client.deleteObject(deleteObjectRequest);
    }

    private String extractFilenameFromUrl(String imageUrl) {
        String[] parts = imageUrl.split("/");
        return parts[3] + "/" + parts[4];
    }
}