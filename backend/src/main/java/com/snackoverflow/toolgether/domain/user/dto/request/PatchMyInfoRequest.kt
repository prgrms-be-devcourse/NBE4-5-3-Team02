package com.snackoverflow.toolgether.domain.user.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

data class PatchMyInfoRequest(
    @field:NotBlank
    @field:Pattern(
        regexp = "^[0-9]+$",
        message = "전화번호는 숫자만 입력해주세요 (하이픈 제외)"
    )
    val phoneNumber: String,

    @field:NotBlank(message = "닉네임을 입력해주세요")
    @field:Pattern(
        regexp = "^[가-힣a-zA-Z0-9]{2,10}$",
        message = "닉네임은 최소 2글자 이상, 최대 10글자 이하로 입력해주세요 (특수문자 제외)"
    )
    val nickname: String,

    @field:Valid
    val baseAddress: String?,

    @field:NotNull(message = "위도를 입력해주세요")
    val latitude: Double,

    @field:NotNull(message = "경도를 입력해주세요")
    val longitude: Double
)