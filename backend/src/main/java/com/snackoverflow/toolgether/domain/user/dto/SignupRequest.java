package com.snackoverflow.toolgether.domain.user.dto;

import com.snackoverflow.toolgether.domain.user.entity.Address;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SignupRequest {

    @NotBlank(message = "사용자 id를 입력해주세요")
    @Size(min = 8, max = 20, message = "사용자 id는 8~20자로 입력해주세요")
    private String username;

    @NotBlank(message = "비밀번호를 입력해주세요")
    // @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d]{8,}$", message = "비밀번호는 영문+숫자 조합 8자 이상이어야 합니다")
    private String password;

    @NotBlank(message = "이메일을 입력해주세요")
    @Email(message = "유효한 이메일 형식이 아닙니다")
    private String email;

    @NotBlank(message = "전화번호를 입력해주세요 [하이픈 제외]")
    private String phoneNumber;

    private String nickname;
    private Address address;
    private Double latitude; // 위도
    private Double longitude; // 경도
}
