package com.snackoverflow.toolgether.domain.post.repository

import com.snackoverflow.toolgether.domain.post.entity.Post
import org.springframework.context.annotation.Primary
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
@Primary
interface PostRepository : JpaRepository<Post, Long>, PostRepositoryCustom
