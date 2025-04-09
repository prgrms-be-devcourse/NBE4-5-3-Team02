package com.snackoverflow.toolgether.domain.postimage.entity

import com.snackoverflow.toolgether.domain.post.entity.Post
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PostImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    var post: Post? = null

    @JoinColumn(nullable = false) // 한 장 이상 필수
    var imageUrl: String? = null // 이미지 파일 이름, uuid로 저장 (기존 postImage -> imageUrl 변경)


}
