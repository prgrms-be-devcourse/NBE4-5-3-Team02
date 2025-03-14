package com.snackoverflow.toolgether.domain.review.controller;

import com.snackoverflow.toolgether.domain.postimage.service.PostImageService;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;
import com.snackoverflow.toolgether.domain.review.dto.request.ReviewRequest;
import com.snackoverflow.toolgether.domain.review.entity.Review;
import com.snackoverflow.toolgether.domain.review.service.ReviewService;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.dto.RsData;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import com.snackoverflow.toolgether.global.filter.Login;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
public class ReviewController {

    private final UserService userService;
    private final ReservationService reservationService;
    private final ReviewService reviewService;


    // 리뷰 작성
    @PostMapping("/create")
    public RsData<Void> postReview(
            @Login CustomUserDetails customUserDetails,
            @RequestBody @Validated ReviewRequest reviewRequest
    ) {
        Long userId = customUserDetails.getUserId();
        User user = userService.findUserById(userId);
        Optional<Reservation> reservation = reservationService.getReservationByIdForReview(reviewRequest.getReservationId());
        if (reservation.isEmpty()) {
            return new RsData<>(
                    "404-1",
                    "존재하지 않는 예약입니다."
            );
        }
        Reservation actualReservation = reservation.get();
        if (actualReservation.getStatus() != ReservationStatus.DONE) {
            return new RsData<>(
                    "400-1",
                    "대여 완료 후 리뷰를 작성할 수 있습니다."
            );
        }
        Optional<Review> temp = reviewService.findByUserIdAndReservationId(actualReservation.getId(), user.getId());
        if (reviewService.existsUserIdAndReservationId(user.getId(), actualReservation.getId())) { // 수정: existsUserIdAndReservationId 사용
            return new RsData<>(
                    "409-1",
                    "이미 작성한 리뷰가 존재합니다"
            );
        }
        reviewService.create(reviewRequest, actualReservation, user);
        return new RsData<>(
                "200-1",
                "리뷰가 완료되었습니다"
        );
    }
}
