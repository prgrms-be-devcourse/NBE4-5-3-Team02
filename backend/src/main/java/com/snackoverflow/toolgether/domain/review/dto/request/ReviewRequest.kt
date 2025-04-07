package com.snackoverflow.toolgether.domain.review.dto.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull
import lombok.Data

data class ReviewRequest (
    @field:NotNull
    val reservationId: Long,
    @field:Min(value = 1)
    @field:Max(value = 5)
    val productScore: Int,
    @field:Min(value = 1)
    @field:Max(value = 5)
    val timeScore: Int,
    @field:Min(value = 1)
    @field:Max(value = 5)
    val kindnessScore: Int
)
