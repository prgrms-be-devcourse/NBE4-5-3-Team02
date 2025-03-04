package com.snackoverflow.toolgether.domain.post.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import com.snackoverflow.toolgether.domain.PriceType;
import com.snackoverflow.toolgether.domain.user.entity.User;

@Entity
@Getter
@Builder
@NoArgsConstructor
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

    @Builder.Default
    private int viewCount = 0; // 조회수 (기본 0)
}
