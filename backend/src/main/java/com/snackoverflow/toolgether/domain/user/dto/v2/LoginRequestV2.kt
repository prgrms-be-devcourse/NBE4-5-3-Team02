package com.snackoverflow.toolgether.domain.user.dto.v2;

import jakarta.validation.constraints.*

data class LoginRequestV2(
    @field:NotBlank(message = "이메일을 입력해주세요")
    @field:Email(message = "유효한 이메일 주소를 입력해주세요")
    val email: String,

    @field:NotBlank(message = "비밀번호를 입력해주세요")
    @field:Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다")
    val password: String,

    var rememberMe: Boolean = false,
)


