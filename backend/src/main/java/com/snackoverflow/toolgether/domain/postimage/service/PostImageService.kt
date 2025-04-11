package com.snackoverflow.toolgether.domain.postimage.service

import com.snackoverflow.toolgether.domain.postimage.entity.PostImage
import com.snackoverflow.toolgether.domain.postimage.repository.PostImageRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PostImageService(
    val postImageRepository: PostImageRepository
) {
    fun getPostImagesByPostId(postId: Long): List<PostImage> {
        return postImageRepository.findByPostId(postId)
    }
}
