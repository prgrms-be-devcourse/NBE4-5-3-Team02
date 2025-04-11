package com.snackoverflow.toolgether.domain.review.service;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import com.snackoverflow.toolgether.domain.postimage.entity.PostImage;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.review.dto.request.ReviewRequest;
import com.snackoverflow.toolgether.domain.review.entity.Review;
import com.snackoverflow.toolgether.domain.review.repository.ReviewRepository;
import com.snackoverflow.toolgether.domain.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

public class ReviewServiceTest {

    private final ReviewRepository reviewRepository = Mockito.mock(ReviewRepository.class);
    private ReviewService reviewService;
    private User user1;
    private User user2;
    private Post post;
    private Reservation reservation;
    private ReviewRequest reviewRequest;

    @BeforeEach
    void setUp() {
        reviewService = new ReviewService(reviewRepository);
        this.user1 = User.createGeneralUser(
                "test1@gmail.com",
                "human123",
                "000-0000-0001",
                "닉네임1",
                "서울시 강남구"
        );
        this.user1.setId(1L);

        this.user2 = User.createGeneralUser(
                "test2@gmail.com",
                "seaman222",
                "000-0000-0002",
                "닉네임2",
                "서울시 강남구 " // Address 객체에서 baseAddress 정보만 사용
        );
        this.user2.setId(2L);

        post = new Post(
                10L,
                user1,
                "Sample Post Title",
                "Sample Content",
                LocalDateTime.now(),
                LocalDateTime.now(),
                Category.TOOL,
                PriceType.DAY,
                10000,
                37.5,
                127.0,
                0,
                new HashSet<PostImage>(),
                new HashSet<PostAvailability>()
        );

        reservation = new Reservation(
                1L,
                post,
                user1,
                user2,
                LocalDateTime.now(),
                LocalDateTime.now(),
                LocalDateTime.now().plusHours(1),
                ReservationStatus.DONE,
                null,
                10000.0
        );

        reviewRequest = new ReviewRequest(
                1L,
                4,
                5,
                4
        );
    }

    @Test
    @DisplayName("해당 리뷰가 존재하면 반환")
    void findByUserIdAndReservationId() {
        Review review = new Review(1L, user2, user1, reservation, 5, 5, 5, LocalDateTime.now());
        when(reviewRepository.findByReviewerIdAndReservationId(user2.getId(), reservation.getId()))
                .thenReturn(Optional.of(review));

        Optional<Review> foundReview = reviewService.findByUserIdAndReservationId(user2.getId(), reservation.getId());

        assertTrue(foundReview.isPresent());
        assertEquals(1L, foundReview.get().getId());
    }

    @Test
    @DisplayName("해당 리뷰가 없으면 빈 Optional을 반환")
    void findByUserIdAndReservationId다() {
        when(reviewRepository.findByReviewerIdAndReservationId(1L, 1L))
                .thenReturn(Optional.empty());

        Optional<Review> foundReview = reviewService.findByUserIdAndReservationId(1L, 1L);

        assertFalse(foundReview.isPresent());
    }

    @Test
    @DisplayName("해당 리뷰가 존재하면 true를 반환")
    void existsUserIdAndReservationIdTrue() {
        when(reviewRepository.existsByReviewerIdAndReservationId(user1.getId(), reservation.getId()))
                .thenReturn(true);

        boolean exists = reviewService.existsUserIdAndReservationId(user1.getId(), reservation.getId());

        assertTrue(exists);
    }

    @Test
    @DisplayName("해당 리뷰가 없으면 false를 반환")
    void existsUserIdAndReservationIdFalse() {
        when(reviewRepository.existsByReviewerIdAndReservationId(1L, 1L))
                .thenReturn(false);

        boolean exists = reviewService.existsUserIdAndReservationId(1L, 1L);

        assertFalse(exists);
    }

    @Test
    @DisplayName("리뷰 생성")
    void create() {
        when(reviewRepository.save(Mockito.any(Review.class))).thenReturn(new Review(1L, user1, user2, reservation, 4, 5, 4, LocalDateTime.now())); // Mock save behavior

        reviewService.create(reviewRequest, reservation, user1);

        Mockito.verify(reviewRepository, Mockito.times(1)).save(Mockito.any(Review.class));
    }

    @Test
    @DisplayName("특정 날짜 이후에 생성된 리뷰만 반환")
    void getReviewsCreatedAfter() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMonthAgo = now.minusMonths(1);

        Review review1 = new Review(1L, user1, user2, reservation, 5, 5, 5, now);
        Review review2 = new Review(2L, user1, user2, reservation, 4, 4, 4, oneMonthAgo);

        when(reviewRepository.findByCreatedAtAfter(oneMonthAgo))
                .thenReturn(java.util.List.of(review1)); // Only return review1

        java.util.List<Review> reviewsAfterOneMonthAgo = reviewService.getReviewsCreatedAfter(oneMonthAgo);

        assertEquals(1, reviewsAfterOneMonthAgo.size());
        assertTrue(reviewsAfterOneMonthAgo.contains(review1));
        assertFalse(reviewsAfterOneMonthAgo.contains(review2));
    }
}