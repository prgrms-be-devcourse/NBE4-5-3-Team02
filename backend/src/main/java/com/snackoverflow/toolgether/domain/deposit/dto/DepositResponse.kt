package com.snackoverflow.toolgether.domain.deposit.dto

data class DepositResponse(
    val id: Long,
    val status: String,
    val reservationId: Long,
    val returnReason: String,
    val amount: Int
)