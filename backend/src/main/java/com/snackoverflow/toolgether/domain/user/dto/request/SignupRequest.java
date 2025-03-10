package com.snackoverflow.toolgether.domain.user.dto.request;

import jakarta.validation.constraints.*;

public record SignupRequest(
        @NotBlank(message = "사용자 id를 입력해주세요")
        @Size(min = 8, max = 20, message = "사용자 id는 8~20자로 입력해주세요")
        String username,

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다")
        String password,

        @NotBlank(message = "비밀번호를 입력해주세요")
        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다")
        String checkPassword,

        @Email @NotBlank String email,
        @NotBlank String nickname,
        @NotBlank String phoneNumber,
        @NotBlank String postalCode,
        @NotBlank String baseAddress,
        String detailAddress,  // null 허용
        @NotNull Double latitude,
        @NotNull Double longitude
) {}