package com.snackoverflow.toolgether.domain.review.repository

import com.snackoverflow.toolgether.domain.review.entity.Review
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.LocalDateTime
import java.util.*

@Repository
interface ReviewRepository : JpaRepository<Review, Long> {
    // userId와 reservationId를 사용하여 Review를 작성했는지 조회
    fun findByReviewerIdAndReservationId(userId: Long, reservationId: Long): Optional<Review>

    // userId와 reservationId를 사용하여 Review가 존재하는지 확인
    fun existsByReviewerIdAndReservationId(userId: Long, reservationId: Long): Boolean

    // 특정 시간 이후 리뷰를 조회
    fun findByCreatedAtAfter(date: LocalDateTime?): List<Review>
}
