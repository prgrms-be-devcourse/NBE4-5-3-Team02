package com.snackoverflow.toolgether.domain.reservation.dto

import java.time.LocalDateTime

@JvmRecord
data class ReservationRequest(
    val postId: Long,
    val renterId: Long,
    val ownerId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val deposit: Double,
    val rentalFee: Double
)