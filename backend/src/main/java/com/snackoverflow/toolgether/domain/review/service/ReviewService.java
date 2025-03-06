package com.snackoverflow.toolgether.domain.review.service;

import com.snackoverflow.toolgether.domain.review.entity.Review;
import com.snackoverflow.toolgether.domain.review.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {
    private final ReviewRepository reviewRepository;

    public Optional<Review> findByUserIdAndReservationId(Long userId, Long reservationId) {
        return reviewRepository.findByReviewer_IdAndReservation_Id(userId, reservationId);
    }
}
