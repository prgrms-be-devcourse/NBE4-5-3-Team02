package com.snackoverflow.toolgether.domain.postimage.entity;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @JoinColumn(nullable = false) // 한 장 이상 필수
    private String imageUrl; // 이미지 파일 이름, uuid로 저장 (기존 postImage -> imageUrl 변경)

    /* TODO: 코틀린 변환 전 임시 게터. 추후 제거하고 사용하는 부분 수정 */
    public String getImageUrl() {
        return this.imageUrl;
    }
    public Long getId() {return id;}
}
