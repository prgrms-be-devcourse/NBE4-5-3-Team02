package com.snackoverflow.toolgether.domain.post.repository

import com.snackoverflow.toolgether.domain.post.dto.PostResponse
import com.snackoverflow.toolgether.domain.post.dto.PostSearchRequest
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

interface PostRepositoryCustom {
    fun searchPosts(request: PostSearchRequest, latitude: Double, longitude: Double, pageable: Pageable): Page<PostResponse>
}
