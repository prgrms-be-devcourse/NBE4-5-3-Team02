package com.snackoverflow.toolgether.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class LoginRequest {

    @NotBlank(message = "아이디를 입력해 주세요")
    private String username;

    @NotBlank(message = "비밀번호를 입력해 주세요")
    @Size(min = 8, max = 20)
    private String password;
}
