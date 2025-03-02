package com.snackoverflow.toolgether.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    private User renter; // 예약 요청자 (대여자)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner; // 물건 소유자

    @Column(nullable = false)
    private LocalDateTime createAt; // 거래 요청 일자

    @Column(nullable = false)
    private LocalDateTime startTime; // 대여 시작 시간

    @Column(nullable = false)
    private LocalDateTime endTime; // 대여 종료 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status; // 예약 상태 (ENUM)

    @Column(columnDefinition = "TEXT")
    private String rejectionReason; // 거절 사유 (status가 REJECTED인 경우)

    @Column(nullable = false)
    private Double amount; // 총 결제 금액
}
