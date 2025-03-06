package com.snackoverflow.toolgether.domain.user.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class VerificationRequest {

        @NotBlank(message = "이메일을 입력해 주세요")
        @Email(message = "유효한 이메일 형식이 아닙니다")
        private String email;

        @NotBlank(message = "인증 코드를 입력해주세요")
        @Size(min = 8, max = 8, message = "인증 코드는 8자리여야 합니다")
        @Pattern(regexp = "^[A-Za-z0-9]{8}$", message = "영문 대소문자와 숫자 조합 8자리를 입력해주세요")
        private String code;

}
