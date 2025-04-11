package com.snackoverflow.toolgether.domain.postavailability.dto

import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability
import lombok.Getter
import java.time.format.DateTimeFormatter

data class PostAvailabilityResponse(
    val postAvailability: PostAvailability,
    val formatter: DateTimeFormatter
) {
    val date: String? = postAvailability.date?.format(formatter)
    val startTime: String? = postAvailability.startTime?.format(formatter)
    val endTime: String? = postAvailability.endTime?.format(formatter)
    val isRecurring: Boolean = postAvailability.isRecurring
    val recurrenceDays: Int = postAvailability.recurrence_days
}
