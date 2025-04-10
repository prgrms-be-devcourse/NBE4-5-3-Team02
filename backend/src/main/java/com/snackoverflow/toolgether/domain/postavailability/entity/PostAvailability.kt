package com.snackoverflow.toolgether.domain.postavailability.entity

import com.snackoverflow.toolgether.domain.post.entity.Post
import jakarta.persistence.*
import lombok.AllArgsConstructor
import lombok.Builder
import lombok.Getter
import lombok.NoArgsConstructor
import java.time.LocalDateTime

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
class PostAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private var post: Post? = null

    @Column(nullable = true) // 반복이 아닌 경우 null 가능
    private var date: LocalDateTime? = null // 거래 가능한 날짜

    @Column(nullable = true)
    private var recurrence_days = 0 // 반복 요일 [월 - 1, 화 - 2, ..., 일 - 7]

    @Column(nullable = false)
    private var startTime: LocalDateTime? = null // 거래 가능 시간 시작

    @Column(nullable = false)
    private var endTime: LocalDateTime? = null // 거래 가능 시간 종료

    @Column(nullable = false)
    @Builder.Default
    private var isRecurring = false // 매주 반복 여부, 기본값 false
}
