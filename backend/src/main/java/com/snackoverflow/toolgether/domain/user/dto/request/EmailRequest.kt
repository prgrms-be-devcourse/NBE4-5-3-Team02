package com.snackoverflow.toolgether.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

data class EmailRequest(
    @field:NotBlank(message = "이메일을 입력해주세요")
    @field:Email(message = "유효한 이메일 형식이 아닙니다")
    val email: String
)
