package com.snackoverflow.toolgether.domain.reservation.dto

import java.time.LocalDateTime

@JvmRecord
data class ReservationResponse(
    val id: Long,
    val status: String,
    val postId: Long,
    val startTime: LocalDateTime,
    val endTime: LocalDateTime,
    val amount: Double,
    val rejectionReason: String,
    val ownerId: Long,
    val renterId: Long
)