package com.snackoverflow.toolgether.domain.review.service;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.review.dto.request.ReviewRequest;
import com.snackoverflow.toolgether.domain.review.entity.Review;
import com.snackoverflow.toolgether.domain.review.repository.ReviewRepository;
import com.snackoverflow.toolgether.domain.user.entity.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReviewService {
    private final ReviewRepository reviewRepository;

    //해당 예약에 유저가 작성한 리뷰 조회
    @Transactional(readOnly = true)
    public Optional<Review> findByUserIdAndReservationId(Long userId, Long reservationId) {
        return reviewRepository.findByReviewerIdAndReservationId(userId, reservationId);
    }

    @Transactional(readOnly = true)
    public boolean existsUserIdAndReservationId(Long userId, Long reservationId) {
        return reviewRepository.existsByReviewerIdAndReservationId(userId, reservationId); // 수정: existsBy 메서드 사용
    }

    @Transactional
    public void create(ReviewRequest reviewRequest, Reservation reservation, User user) {
        User reviewee = reservation.getRenter().getId() == user.getId() ? reservation.getOwner() : reservation.getRenter();
        User reviewer = user; // 리뷰어는 항상 현재 사용자

        Review review = new Review(
                null,
                reviewer,
                reviewee,
                reservation,
                reviewRequest.getProductScore(),
                reviewRequest.getTimeScore(),
                reviewRequest.getKindnessScore(),
                null
                );
//        Review review = Review.builder()
//                .reviewer(reviewer)
//                .reviewee(reviewee)
//                .reservation(reservation)
//                .productScore(reviewRequest.getProductScore())
//                .timeScore(reviewRequest.getTimeScore())
//                .kindnessScore(reviewRequest.getKindnessScore())
//                .build();
        reviewRepository.save(review);
    }

    public List<Review> getReviewsCreatedAfter(LocalDateTime oneYearAgo) {
        return reviewRepository.findByCreatedAtAfter(oneYearAgo);
    }
}
