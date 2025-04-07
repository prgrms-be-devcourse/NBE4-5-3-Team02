package com.snackoverflow.toolgether.domain.review.entity

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation
import com.snackoverflow.toolgether.domain.user.entity.User
import jakarta.persistence.*
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.LocalDateTime

@Entity
@EntityListeners(AuditingEntityListener::class)
data class Review(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    val reviewer: User, // 리뷰 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    val reviewee: User, // 리뷰 대상자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    val reservation: Reservation,

    @Column(nullable = false)
    @field:Min(1)
    @field:Max(5)
    var productScore: Int = 1, // [1~5], 제품 상태 평가 점수

    @Column(nullable = false)
    @field:Min(1)
    @field:Max(5)
    var timeScore: Int = 1, // 시간 약속 평가 점수

    @Column(nullable = false)
    @field:Min(1)
    @field:Max(5)
    var kindnessScore: Int = 1, // 응대 친절도 평가 점수

    @CreatedDate
    val createdAt: LocalDateTime? = null // 리뷰 작성 날짜
)