package com.snackoverflow.toolgether.domain.postavailability.entity;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PostAvailability {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Column(nullable = true) // 반복이 아닌 경우 null 가능
    private LocalDateTime date; // 거래 가능한 날짜

    @Column(nullable = true)
    private int recurrence_days; // 반복 요일 [월 - 1, 화 - 2, ..., 일 - 7]

    @Column(nullable = false)
    private LocalDateTime startTime; // 거래 가능 시간 시작

    @Column(nullable = false)
    private LocalDateTime endTime; // 거래 가능 시간 종료

    @Column(nullable = false)
    @Builder.Default
    private boolean isRecurring = false; // 매주 반복 여부, 기본값 false

    /* TODO : 임시 */
    public PostAvailability(
            Post post,
            LocalDateTime date,
            int recurrence_days,
            LocalDateTime startTime,
            LocalDateTime endTime,
            boolean isRecurring
    ) {
        this.post = post;
        this.date = date;
        this.recurrence_days = recurrence_days;
        this.startTime = startTime;
        this.endTime = endTime;
        this.isRecurring = isRecurring;
    }
}