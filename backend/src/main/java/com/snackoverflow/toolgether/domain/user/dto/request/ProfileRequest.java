package com.snackoverflow.toolgether.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.Getter;

@Data
@Getter
public class ProfileRequest {
    @NotBlank
    private String uuid;
}
