package com.snackoverflow.toolgether.domain.user.dto.v2;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

data class SignupRequestV2(
    @field:NotBlank(message = "이메일을 입력해주세요")
    @field:Email(message = "유효한 이메일 주소를 입력해주세요")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
        message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다"
    )
    val password: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요")
    @field:Pattern(
        regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$",
        message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다"
    )
    val checkPassword: String,

    @field:NotBlank(message = "닉네임을 입력해주세요") val nickname: String,
    @field:NotBlank val phoneNumber: String,
    @field:NotNull val latitude: Double,
    @field:NotNull val longitude: Double
)