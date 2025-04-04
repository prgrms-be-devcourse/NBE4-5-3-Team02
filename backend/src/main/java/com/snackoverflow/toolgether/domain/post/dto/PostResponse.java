package com.snackoverflow.toolgether.domain.post.dto;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.postavailability.dto.PostAvailabilityResponse;
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import javax.annotation.Nullable;
import java.time.format.DateTimeFormatter;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
@Slf4j
public class PostResponse {
    private Long id;
    @Nullable private Long userid;
    @Nullable private String nickname;
    private String title;
    private String content;
    private String category;
    private String priceType;
    private int price;
    private Double latitude;
    private Double longitude;
    private String createdAt;  // 날짜 포맷 적용
    private int viewCount;
    private Set<String> images;
    private Set<PostAvailabilityResponse> availabilities;

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public PostResponse(Post post, Set<String> images, Set<PostAvailability> availabilities) {
        // this.id = post.getId();
        this.userid = post.getUser().getId();
        this.nickname = post.getUser().getNickname();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.category = post.getCategory().name();
        this.priceType = post.getPriceType().name();
        this.price = post.getPrice();
        this.latitude = post.getLatitude();
        this.longitude = post.getLongitude();
        this.viewCount = post.getViewCount();
        this.createdAt = post.getCreatedAt() != null ? post.getCreatedAt().format(FORMATTER) : null;

        //images와 availabilities를 Set으로 유지
        this.images = images;
        this.availabilities = availabilities.stream()
                .map(avail -> new PostAvailabilityResponse(avail, FORMATTER))
                .collect(Collectors.toSet());
    }

    //빈 Set을 기본값으로 사용하여 다른 생성자 호출
    public PostResponse(Post post) {
        this(post, Set.of(), Set.of());
    }
}
