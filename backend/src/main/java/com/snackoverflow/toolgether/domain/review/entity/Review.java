package com.snackoverflow.toolgether.domain.review.entity;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.user.entity.User;
import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Review {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer; // 리뷰 작성자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee; // 리뷰 대상자

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private int productScore; // [1~5], 제품 상태 평가 점수

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer timeScore; // 시간 약속 평가 점수

    @Min(1)
    @Max(5)
    @Column(nullable = false)
    private Integer kindnessScore; // 응대 친절도 평가 점수

    @CreatedDate
    private LocalDateTime createdAt; // 리뷰 작성 날짜
}
