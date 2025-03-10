package com.snackoverflow.toolgether.domain.user.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class KakaoGeoResponse {

    @JsonProperty("documents")
    private List<Document> documents;

    @JsonProperty("meta")
    private Meta meta;

    @Data
    public static class Document {

        @JsonProperty("address_name")
        private String addressName;

        @JsonProperty("x")
        private String longitude;

        @JsonProperty("y")
        private String latitude;
    }

    @Data
    public static class Meta {

        @JsonProperty("is_end")
        private boolean isEnd;

        @JsonProperty("pageable_count")
        private int pageableCount;

        @JsonProperty("total_count")
        private int totalCount;
    }
}