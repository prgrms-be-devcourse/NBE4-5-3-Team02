package com.snackoverflow.toolgether.domain.post.service;

import com.snackoverflow.toolgether.domain.post.dto.PostCreateRequest;
import com.snackoverflow.toolgether.domain.post.dto.PostResponse;
import com.snackoverflow.toolgether.domain.post.dto.PostSearchRequest;
import com.snackoverflow.toolgether.domain.post.dto.PostUpdateRequest;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.user.entity.User;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;


public interface PostService {
    PostResponse createPost(User user, PostCreateRequest request, List<MultipartFile> images);

    PostResponse getPostById(Long postId);

    void deletePost(Long postId);

    PostResponse updatePost(Long postId, @Valid PostUpdateRequest request);

    Post findPostById(Long aLong); // 예약에 필요한 메서드

    Page<PostResponse> searchPosts(PostSearchRequest request, Pageable pageable);
}
