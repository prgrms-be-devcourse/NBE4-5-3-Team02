package com.snackoverflow.toolgether.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class AdditionalInfoRequest(
    @field:NotBlank
    @field:Pattern(
        regexp = "^[0-9]+$",
        message = "전화번호는 숫자만 입력해주세요 (하이픈 제외)"
    )
    val phoneNumber: String,
    @field:NotBlank val latitude: Double,
    @field:NotBlank val longitude: Double,
)