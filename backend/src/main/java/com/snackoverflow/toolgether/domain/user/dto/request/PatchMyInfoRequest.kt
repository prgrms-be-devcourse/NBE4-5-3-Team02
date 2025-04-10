package com.snackoverflow.toolgether.domain.user.dto.request;

import com.snackoverflow.toolgether.domain.user.entity.Address;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class PatchMyInfoRequest {
    @NotBlank(message = "전화번호를 입력해주세요 [하이픈 제외]")
    private String phoneNumber;

    @NotBlank(message = "닉네임을 입력해주세요")
    @Pattern(regexp = "^[가-힣]{4,10}$", message = "닉네임은 한글 4~10자로 입력해주세요")
    private String nickname;

    @Valid
    private Address address;

    @NotNull
    private Double latitude; // 위도

    @NotNull
    private Double longitude; // 경도
}
