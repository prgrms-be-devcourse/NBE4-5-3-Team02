package com.snackoverflow.toolgether.domain.postimage.service;

import com.snackoverflow.toolgether.domain.postimage.entity.PostImage;
import com.snackoverflow.toolgether.domain.postimage.repository.PostImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PostImageService {
    private final PostImageRepository postImageRepository;

    public List<PostImage> getPostImagesByPostId(Long postId) {
        return postImageRepository.findByPostId(postId);
    }
}
