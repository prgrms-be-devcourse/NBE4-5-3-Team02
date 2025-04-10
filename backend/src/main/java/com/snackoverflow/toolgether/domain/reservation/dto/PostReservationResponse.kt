package com.snackoverflow.toolgether.domain.reservation.dto

data class PostReservationResponse(
    val id: Long,
    val userId: Long?,
    val title: String,
    val priceType: String,
    val price: Int
)