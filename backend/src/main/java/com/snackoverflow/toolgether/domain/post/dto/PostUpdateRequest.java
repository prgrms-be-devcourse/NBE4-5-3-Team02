package com.snackoverflow.toolgether.domain.post.dto;

import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import com.snackoverflow.toolgether.domain.postavailability.dto.PostAvailabilityRequest;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class PostUpdateRequest {

    @NotBlank(message = "제목을 입력해야 합니다.")
    private String title; // 제목

    @NotBlank(message = "내용을 입력해야 합니다.")
    private String content; // 내용

    @NotNull(message = "카테고리를 선택해야 합니다.")
    private Category category; // 카테고리 (TOOL, ELECTRONICS)

    @NotNull(message = "가격 유형을 선택해야 합니다.")
    private PriceType priceType; // 가격 유형 (일 / 시간)

    @Positive(message = "가격은 0보다 커야 합니다.")
    private int price; // 총 가격

    @NotNull(message = "위도를 입력해야 합니다.")
    private Double latitude; // 위도

    @NotNull(message = "경도를 입력해야 합니다.")
    private Double longitude; // 경도

    @NotNull
    @Positive
    private int viewCount; // 조회수

    private List<String> images; // 이미지 리스트 (UUID 또는 URL)

    private List<PostAvailabilityRequest> availabilities; // 거래 가능 일정 리스트
}
