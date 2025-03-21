package com.snackoverflow.toolgether.domain.post.dto;

import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import lombok.*;

import javax.annotation.Nullable;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class PostSearchRequest {

    @Nullable String userId;
    private String keyword;      // 제목 또는 내용 키워드
    private Category category;   // 카테고리 필터
    private PriceType priceType; // 가격 유형 필터
    private Integer minPrice;   // 최소 가격
    private Integer maxPrice;   // 최대 가격
    private Double latitude;     // 위도
    private Double longitude;    // 경도
    private Double distance;     // 검색 반경 (km) ex) 1km,3km,5km

}
