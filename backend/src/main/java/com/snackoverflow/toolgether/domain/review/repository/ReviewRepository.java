package com.snackoverflow.toolgether.domain.review.repository;

import com.snackoverflow.toolgether.domain.review.entity.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
    // userId와 reservationId를 사용하여 Review를 작성했는지 조회
    Optional<Review> findByReviewerIdAndReservationId(Long userId, Long reservationId);

    // userId와 reservationId를 사용하여 Review가 존재하는지 확인
    boolean existsByReviewerIdAndReservationId(Long userId, Long reservationId);

    // 특정 시간 이후 리뷰를 조회
    List<Review> findByCreatedAtAfter(LocalDateTime date);
}
