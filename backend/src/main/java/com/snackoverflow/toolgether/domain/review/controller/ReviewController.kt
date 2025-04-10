package com.snackoverflow.toolgether.domain.review.controller

import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService
import com.snackoverflow.toolgether.domain.review.dto.request.ReviewRequest
import com.snackoverflow.toolgether.domain.review.service.ReviewService
import com.snackoverflow.toolgether.domain.user.service.UserService
import com.snackoverflow.toolgether.global.dto.RsData
import com.snackoverflow.toolgether.global.filter.CustomUserDetails
import com.snackoverflow.toolgether.global.filter.Login
import lombok.RequiredArgsConstructor
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/review")
@RequiredArgsConstructor
class ReviewController(
    private val userService: UserService,
    private val reservationService: ReservationService,
    private val reviewService: ReviewService,
) {



    // 리뷰 작성
    @PostMapping("/create")
    fun postReview(
        @Login customUserDetails: CustomUserDetails,
        @RequestBody @Validated reviewRequest: ReviewRequest
    ): RsData<Void> {
        val userId = customUserDetails.userId
        val user = userService.findUserById(userId)
        val reservation = reservationService.getReservationByIdForReview(reviewRequest.reservationId)
        if (reservation.isEmpty) {
            return RsData(
                "404-1",
                "존재하지 않는 예약입니다."
            )
        }

        val actualReservation = reservation.get()
        if (actualReservation.status != ReservationStatus.DONE) {
            return RsData(
                "400-1",
                "대여 완료 후 리뷰를 작성할 수 있습니다."
            )
        }

        val temp = reviewService.findByUserIdAndReservationId(actualReservation.id!!, user.id!!)
        if (reviewService.existsUserIdAndReservationId(
                user.id,
                actualReservation.id!!
            )
        ) { // 수정: existsUserIdAndReservationId 사용
            return RsData(
                "409-1",
                "이미 작성한 리뷰가 존재합니다"
            )
        }

        reviewService.create(reviewRequest, actualReservation, user)

        return RsData(
            "200-1",
            "리뷰가 완료되었습니다"
        )
    }
}
