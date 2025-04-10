package com.snackoverflow.toolgether.domain.user.dto.v2;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;


data class KakaoGeoResponseV2(
    @JsonProperty("documents")
    val documents: List<Document>
) {
    data class Document(
        @JsonProperty("road_address")
        val roadAddress: RoadAddress?
    )

    data class RoadAddress(
        @JsonProperty("region_1depth_name")
        val region1: String,

        @JsonProperty("region_2depth_name")
        val region2: String
    )
}