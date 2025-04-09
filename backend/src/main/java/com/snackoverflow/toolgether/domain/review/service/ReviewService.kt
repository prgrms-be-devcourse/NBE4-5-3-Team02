package com.snackoverflow.toolgether.domain.review.service

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation
import com.snackoverflow.toolgether.domain.review.dto.request.ReviewRequest
import com.snackoverflow.toolgether.domain.review.entity.Review
import com.snackoverflow.toolgether.domain.review.repository.ReviewRepository
import com.snackoverflow.toolgether.domain.user.entity.User
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*

@Service
@RequiredArgsConstructor
class ReviewService (
    private val reviewRepository: ReviewRepository
) {


    //해당 예약에 유저가 작성한 리뷰 조회
    @Transactional(readOnly = true)
    fun findByUserIdAndReservationId(userId: Long, reservationId: Long): Optional<Review> {
        return reviewRepository.findByReviewerIdAndReservationId(userId, reservationId)
    }

    @Transactional(readOnly = true)
    fun existsUserIdAndReservationId(userId: Long, reservationId: Long): Boolean {
        return reviewRepository.existsByReviewerIdAndReservationId(userId, reservationId) // 수정: existsBy 메서드 사용
    }

    @Transactional
    fun create(reviewRequest: ReviewRequest, reservation: Reservation, user: User) {
        val reviewee = if (reservation.renter.getId() == user.getId()) reservation.owner else reservation.renter
        val reviewer = user // 리뷰어는 항상 현재 사용자

        val review = Review(
            null,
            reviewer,
            reviewee,
            reservation,
            reviewRequest.productScore,
            reviewRequest.timeScore,
            reviewRequest.kindnessScore,
            null
        )
        //        Review review = Review.builder()
//                .reviewer(reviewer)
//                .reviewee(reviewee)
//                .reservation(reservation)
//                .productScore(reviewRequest.getProductScore())
//                .timeScore(reviewRequest.getTimeScore())
//                .kindnessScore(reviewRequest.getKindnessScore())
//                .build();
        reviewRepository.save(review)
    }

    fun getReviewsCreatedAfter(oneYearAgo: LocalDateTime?): List<Review> {
        return reviewRepository.findByCreatedAtAfter(oneYearAgo)
    }
}
