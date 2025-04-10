package com.snackoverflow.toolgether.domain.postavailability.repository

import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface PostAvailabilityRepository : JpaRepository<PostAvailability, Long> {
    fun findAllByPostId(postId: Long): List<PostAvailability>

    fun deleteByPostId(postId: Long)
}
