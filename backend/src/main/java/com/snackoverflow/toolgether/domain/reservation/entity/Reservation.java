package com.snackoverflow.toolgether.domain.reservation.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.user.entity.User;

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

    /* TODO: 코틀린 변환 전 임시 게터. 추후 제거하고 사용하는 부분 수정 */
    public User getRenter() {
        return this.renter;
    }

    public User getOwner() {
        return this.owner;
    }

    public ReservationStatus getStatus() {
        return this.status;
    }

    public Long getId() {
        return this.id;
    }

    /* status 변경 함수 */
    // 요청됨 상태 변경
    public void approve() {
        if (this.status != ReservationStatus.REQUESTED) {
            throw new IllegalStateException("요청된 예약만 수락할 수 있습니다.");
        }
        this.status = ReservationStatus.APPROVED;
    }

    // 진행 중
    public void startRental() {
        if (this.status != ReservationStatus.APPROVED) {
            throw new IllegalStateException("수락된 예약만 대여할 수 있습니다.");
        }
        this.status = ReservationStatus.IN_PROGRESS;
    }

    // 완료
    public void completeRental() {
        if (this.status != ReservationStatus.IN_PROGRESS) {
            throw new IllegalStateException("대여 중인 예약만 완료 가능합니다.");
        }
        this.status = ReservationStatus.DONE;
    }

    // 거절 -> 사유 포함
    public void reject(String reason) {
        if (this.status != ReservationStatus.REQUESTED) {
            throw new IllegalStateException("요청된 예약만 거절할 수 있습니다.");
        }
        this.status = ReservationStatus.REJECTED;
        this.rejectionReason = reason;
    }

    // 소유자 문제로 실패함
    public void failDueToOwnerIssue() {
        if (this.status != ReservationStatus.APPROVED && this.status != ReservationStatus.IN_PROGRESS) {
            throw new IllegalStateException("수락되었거나 진행 중인 예약만 실패 처리가 가능합니다.");
        }
        this.status = ReservationStatus.FAILED_OWNER_ISSUE;
    }

    // 대여자 문제로 실패함
    public void failDueToRenterIssue() {
        if (this.status != ReservationStatus.APPROVED && this.status != ReservationStatus.IN_PROGRESS) {
            throw new IllegalStateException("수락되었거나 진행 중인 실패 처리가 가능합니다.");
        }
        this.status = ReservationStatus.FAILED_RENTER_ISSUE;
    }

    // 예약 요청 취소
    public void canceled(){
        if(this.status != ReservationStatus.REQUESTED){
            throw new IllegalStateException("요청 대기 상태에서만 취소 처리가 가능합니다.");
        }
        this.status = ReservationStatus.CANCELED;
    }
}
