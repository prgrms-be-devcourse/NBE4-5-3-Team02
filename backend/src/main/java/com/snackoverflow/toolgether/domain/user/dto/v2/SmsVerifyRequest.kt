package com.snackoverflow.toolgether.domain.user.dto.v2;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

data class SmsVerifyRequest(
        @field:NotBlank(message = "전화번호를 입력해주세요")
@field:Pattern(regexp = "^[0-9]+$", message = "전화번호는 숫자만 입력해주세요 (하이픈 제외)")
val phoneNumber: String,

@field:NotBlank(message = "인증 코드를 입력해 주세요")
@field:Pattern(regexp = "^[0-9]{6}$", message = "인증 코드는 6자리 숫자여야 합니다")
val code: String
)