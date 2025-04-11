package com.snackoverflow.toolgether.domain.post.dto

import com.snackoverflow.toolgether.domain.post.entity.enums.Category
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType
import lombok.AllArgsConstructor
import lombok.Data
import lombok.NoArgsConstructor

data class PostSearchRequest (
    var userId: String? = null,
    var keyword: String, // 제목 또는 내용 키워드
    var category: Category, // 카테고리 필터
    var priceType: PriceType, // 가격 유형 필터
    var minPrice: Int, // 최소 가격
    var maxPrice: Int, // 최대 가격
    var latitude: Double, // 위도
    var longitude: Double, // 경도
    var distance: Double // 검색 반경 (km) ex) 1km,3km,5km
)
