package com.snackoverflow.toolgether.domain.post.dto;

import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.postavailability.dto.PostAvailabilityRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;

import java.util.List;

@Getter
public class PostCreateRequest {

    @NotBlank
    private String title;

    @NotBlank
    private String content;

    @NotNull
    private Category category;

    @NotNull
    private PriceType priceType;

    @NotNull
    private Integer price;

    @NotNull
    private Double latitude;

    @NotNull
    private Double longitude;

    @Size(min = 1, max = 3, message = "이미지는 최소 1장, 최대 3장까지 등록해야 합니다.")
    private List<String> images;

    @Size(min = 1, message = "최소 한 개 이상의 스케줄을 등록해야 합니다.")
    private List<PostAvailabilityRequest> availabilities;
}

