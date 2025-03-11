package com.snackoverflow.toolgether.global.util.s3;

import org.springframework.web.multipart.MultipartFile;

public interface S3Service {
    String upload(MultipartFile multipartFile, String dirName);
    void delete(String imageUrl);
}