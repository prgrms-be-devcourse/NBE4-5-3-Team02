package com.snackoverflow.toolgether.domain.post.dto

import com.snackoverflow.toolgether.domain.post.entity.enums.Category
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType
import com.snackoverflow.toolgether.domain.postavailability.dto.PostAvailabilityRequest
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import lombok.Builder
import lombok.Getter

class PostUpdateRequest (
    val title: @NotBlank(message = "제목을 입력해야 합니다.") String, // 제목
    val content: @NotBlank(message = "내용을 입력해야 합니다.") String, // 내용
    val category: @NotNull(message = "카테고리를 선택해야 합니다.") Category, // 카테고리 (TOOL, ELECTRONICS)
    val priceType: @NotNull(message = "가격 유형을 선택해야 합니다.") PriceType, // 가격 유형 (일 / 시간)
    val price: @Positive(message = "가격은 0보다 커야 합니다.") Int, // 총 가격
    val latitude: @NotNull(message = "위도를 입력해야 합니다.") Double, // 위도
    val longitude: @NotNull(message = "경도를 입력해야 합니다.") Double, // 경도
    val viewCount: @NotNull @Positive Int, // 조회수
    val images: List<String>? = null, // 이미지 리스트 (UUID 또는 URL)
    val availabilities: List<PostAvailabilityRequest>? = null // 거래 가능 일정 리스트
)
