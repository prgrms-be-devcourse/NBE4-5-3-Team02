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

    @field:NotBlank(message = "닉네임을 입력해주세요")
    @field:Pattern(
        regexp = "^[가-힣a-zA-Z0-9]{2,10}$",
        message = "닉네임은 최소 2글자 이상, 최대 10글자 이하로 입력해주세요 (특수문자 제외)"
    )
    val nickname: String,

    @field:NotBlank @field:Pattern(
        regexp = "^[0-9]+$",
        message = "전화번호는 숫자만 입력해주세요 (하이픈 제외)"
    )
    val phoneNumber: String,
    @field:NotNull val latitude: Double,
    @field:NotNull val longitude: Double
)