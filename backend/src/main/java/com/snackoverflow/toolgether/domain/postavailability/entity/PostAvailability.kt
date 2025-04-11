package com.snackoverflow.toolgether.domain.postavailability.entity

import com.snackoverflow.toolgether.domain.post.entity.Post
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
data class PostAvailability (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    var post: Post,

    @Column(nullable = true) // 반복이 아닌 경우 null 가능
    var date: LocalDateTime? = null, // 거래 가능한 날짜

    @Column(nullable = true)
    var recurrence_days: Int = 0, // 반복 요일 [월 - 1, 화 - 2, ..., 일 - 7]

    @Column(nullable = false)
    var startTime: LocalDateTime, // 거래 가능 시간 시작

    @Column(nullable = false)
    var endTime: LocalDateTime, // 거래 가능 시간 종료,

    @Column(nullable = false)
    var isRecurring: Boolean = false // 매주 반복 여부, 기본값 false
) {
    protected constructor() : this(
        post = Post(),
        date = null,
        recurrence_days = 0,
        startTime = LocalDateTime.now(), // 현재 시간으로 초기화
        endTime = LocalDateTime.now(),   // 현재 시간으로 초기화
        isRecurring = false
    )
}
