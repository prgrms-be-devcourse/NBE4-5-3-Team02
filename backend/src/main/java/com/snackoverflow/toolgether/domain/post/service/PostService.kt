package com.snackoverflow.toolgether.domain.post.service

import com.snackoverflow.toolgether.domain.post.dto.PostCreateRequest
import com.snackoverflow.toolgether.domain.post.dto.PostResponse
import com.snackoverflow.toolgether.domain.post.dto.PostSearchRequest
import com.snackoverflow.toolgether.domain.post.dto.PostUpdateRequest
import com.snackoverflow.toolgether.domain.post.entity.Post
import com.snackoverflow.toolgether.domain.user.entity.User
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.web.multipart.MultipartFile

interface PostService {
    fun createPost(user: User, request: PostCreateRequest, images: List<MultipartFile>): PostResponse

    fun getPostById(postId: Long): PostResponse

    fun deletePost(postId: Long)

    fun updatePost(postId: Long, request: @Valid PostUpdateRequest): PostResponse?

    fun findPostById(postId: Long): Post // 예약에 필요한 메서드

    fun searchPosts(request: PostSearchRequest, pageable: Pageable): Page<PostResponse>
}
