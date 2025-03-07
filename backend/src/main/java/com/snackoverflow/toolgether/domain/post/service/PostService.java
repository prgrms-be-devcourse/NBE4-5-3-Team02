package com.snackoverflow.toolgether.domain.post.service;

import com.snackoverflow.toolgether.domain.post.dto.PostCreateRequest;
import com.snackoverflow.toolgether.domain.post.dto.PostResponse;
import com.snackoverflow.toolgether.domain.post.dto.PostUpdateRequest;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.repository.PostRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public interface PostService {
    PostResponse createPost(PostCreateRequest request);

    PostResponse getPostById(Long postId);

    void deletePost(Long postId);

    PostResponse updatePost(Long postId, @Valid PostUpdateRequest request);

    Post findPostById(Long aLong); // 예약에 필요한 메서드
}
