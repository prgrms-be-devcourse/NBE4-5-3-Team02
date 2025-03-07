package com.snackoverflow.toolgether.domain.post.dto;

import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.postavailability.dto.PostAvailabilityResponse;
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
public class PostResponse {
    private Long id;
    private String title;
    private String content;
    private String category;
    private String priceType;
    private int price;
    private Double latitude;
    private Double longitude;
    private LocalDateTime createdAt;
    private int viewCount;
    private List<String> images;
    private List<PostAvailabilityResponse> availabilities;

    public PostResponse(Post post, List<String> images, List<PostAvailability> availabilities) {
        this.id = post.getId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory().name();
        this.priceType = post.getPriceType().name();
        this.price = post.getPrice();
        this.latitude = post.getLatitude();
        this.longitude = post.getLongitude();
        this.viewCount = post.getViewCount();
        this.images = images;
        this.availabilities = availabilities.stream()
                .map(PostAvailabilityResponse::new)
                .toList(); // PostAvailability → PostAvailabilityResponse 변환
    }

    public PostResponse(Post post) {
        this(post, List.of(), List.of());
    }

}
