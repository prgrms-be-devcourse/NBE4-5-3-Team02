package com.snackoverflow.toolgether.domain.review.repository;

import com.snackoverflow.toolgether.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // userId와 reservationId를 사용하여 Review를 작성했는지 조회
    Optional<Review> findByReviewer_IdAndReservation_Id(Long userId, Long reservationId);
}
