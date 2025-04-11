package com.snackoverflow.toolgether.domain.postimage.repository

import com.snackoverflow.toolgether.domain.postimage.entity.PostImage
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PostImageRepository : JpaRepository<PostImage, Long> {
    fun findAllByPostId(postId: Long): List<PostImage> // 특정 게시물의 이미지 리스트 조회

    fun findByPostId(postId: Long): List<PostImage>
    fun deleteByPostId(postId: Long)

    @Query("SELECT pi.imageUrl FROM PostImage pi")
    fun findAllImageUrl(): List<String>
}
