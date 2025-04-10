package com.snackoverflow.toolgether.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AdditionalInfoRequest(
        @NotBlank String phoneNumber,
        @NotBlank String postalCode,
        @NotBlank String baseAddress,
        String detailAddress,  // null 허용
        @NotNull Double latitude,
        @NotNull Double longitude
) {}