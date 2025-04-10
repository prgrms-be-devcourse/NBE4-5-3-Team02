package com.snackoverflow.toolgether.domain.post.entity;

import com.snackoverflow.toolgether.domain.post.entity.enums.Category;
import com.snackoverflow.toolgether.domain.post.entity.enums.PriceType;
import com.snackoverflow.toolgether.domain.postavailability.entity.PostAvailability;
import com.snackoverflow.toolgether.domain.postimage.entity.PostImage;
import com.snackoverflow.toolgether.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.BatchSize;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Getter
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Post {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = false)
    private String title; // 제목

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content; // 내용

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt; // 글 작성 시간

    @UpdateTimestamp
    private LocalDateTime updateAt; // 글 수정 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category; // 카테고리 (TOOL, ELECTRONICS)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PriceType priceType; // 가격 유형 (일 / 시간)

    @Column(nullable = false)
    private int price; // 총 가격

    @Column(nullable = false)
    private Double latitude; // 위도

    @Column(nullable = false)
    private Double longitude; // 경도

    private int viewCount = 0; // 조회수 (기본 0)

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private Set<PostImage> postImages = new HashSet<>(); // 이미지

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @BatchSize(size = 10)
    private Set<PostAvailability> postAvailabilities = new HashSet<>(); // 스케줄

    private Post(User user, String title, String content, Category category, PriceType priceType, int price, Double latitude, Double longitude) {
        this.user = user;
        this.title = title;
        this.content = content;
        this.category = category;
        this.priceType = priceType;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.createdAt = LocalDateTime.now(); // 자동 설정
        this.viewCount = 0; // 기본값
    }

    public void updatePost(String title, String content, Category category, PriceType priceType, int price, Double latitude, Double longitude, int viewCount) {
        this.title = title;
        this.content = content;
        this.category = category;
        this.priceType = priceType;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.viewCount = viewCount;
    }

    public static Post createPost(User user, String title, String content, Category category, PriceType priceType, int price, Double latitude, Double longitude) {
        return new Post(user, title, content, category, priceType, price, latitude, longitude);
    }

    public void incrementViewCount() {
        this.viewCount += 1;
    }

    public void updatePostAvailability(Set<PostAvailability> postAvailabilities) {
        this.postAvailabilities = postAvailabilities;
    }

    /* TODO : 마이그레이션 이후 삭제 */

    public Post(
        User user,
        String title,
        String content,
        LocalDateTime createdAt,
        LocalDateTime updateAt,
        Category category,
        PriceType priceType,
        int price,
        Double latitude,
        Double longitude,
        int viewCount
    ){
        this.user = user;
        this.title = title;
        this.content = content;
        this.createdAt = createdAt;
        this.updateAt = updateAt;
        this.category = category;
        this.priceType = priceType;
        this.price = price;
        this.latitude = latitude;
        this.longitude = longitude;
        this.viewCount = viewCount;
    }

    public Post(){}

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public User getUser() {
        return user;
    }

    public PriceType getPriceType() {
        return priceType;
    }

    public int getPrice() {
        return price;
    }
}
