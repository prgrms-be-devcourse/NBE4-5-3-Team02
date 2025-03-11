package com.snackoverflow.toolgether.domain.user.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Data
@Getter
public class ProfileRequest {
    @NotNull
    private MultipartFile profileImage;
}
