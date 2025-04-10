package com.snackoverflow.toolgether.domain.user.dto.response;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import kotlin.jvm.JvmStatic;

import java.time.LocalDateTime;

data class MyReservationInfoResponse(
    val id: Long?,
    val title: String,
    val image: String?,
    val amount: Double,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val status: String,
    val isReviewed: Boolean
) {
    companion object {
        @JvmStatic
        fun from(reservation: Reservation, imageUrl: String?, isReviewed: Boolean): MyReservationInfoResponse {
            return MyReservationInfoResponse(
                    id = reservation.id,
                    title = reservation.post.title,
                    image = imageUrl,
                    amount = reservation.amount,
                    startTime = reservation.startTime,
                    endTime = reservation.endTime,
                    status = reservation.status.toString(),
                    isReviewed = isReviewed
            )
        }
    }
}