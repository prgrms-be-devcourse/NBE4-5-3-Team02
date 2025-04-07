package com.snackoverflow.toolgether.domain.reservation.entity

import com.snackoverflow.toolgether.domain.post.entity.Post
import com.snackoverflow.toolgether.domain.user.entity.User
import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    lateinit var post: Post

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "renter_id", nullable = false)
    lateinit var renter: User // 예약 요청자 (대여자)

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    lateinit var owner: User // 물건 소유자

    @Column(nullable = false)
    lateinit var createAt: LocalDateTime // 거래 요청 일자

    @Column(nullable = false)
    lateinit var startTime: LocalDateTime // 대여 시작 시간

    @Column(nullable = false)
    lateinit var endTime: LocalDateTime // 대여 종료 시간

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var status: ReservationStatus // 예약 상태 (ENUM)

    @Column(columnDefinition = "TEXT")
    lateinit var rejectionReason: String // 거절 사유 (status가 REJECTED인 경우)

    @Column(nullable = false)
    var amount: Double = 0.0 // 총 결제 금액



    /* status 변경 함수 */ // 요청됨 상태 변경
    fun approve() {
        check(this.status == ReservationStatus.REQUESTED) { "요청된 예약만 수락할 수 있습니다." }
        this.status = ReservationStatus.APPROVED
    }

    // 진행 중
    fun startRental() {
        check(this.status == ReservationStatus.APPROVED) { "수락된 예약만 대여할 수 있습니다." }
        this.status = ReservationStatus.IN_PROGRESS
    }

    // 완료
    fun completeRental() {
        check(this.status == ReservationStatus.IN_PROGRESS) { "대여 중인 예약만 완료 가능합니다." }
        this.status = ReservationStatus.DONE
    }

    // 거절 -> 사유 포함
    fun reject(reason: String?) {
        check(this.status == ReservationStatus.REQUESTED) { "요청된 예약만 거절할 수 있습니다." }
        this.status = ReservationStatus.REJECTED
        if (reason != null) {
            this.rejectionReason = reason
        }
    }

    // 소유자 문제로 실패함
    fun failDueToOwnerIssue() {
        check(!(this.status != ReservationStatus.APPROVED && this.status != ReservationStatus.IN_PROGRESS)) { "수락되었거나 진행 중인 예약만 실패 처리가 가능합니다." }
        this.status = ReservationStatus.FAILED_OWNER_ISSUE
    }

    // 대여자 문제로 실패함
    fun failDueToRenterIssue() {
        check(!(this.status != ReservationStatus.APPROVED && this.status != ReservationStatus.IN_PROGRESS)) { "수락되었거나 진행 중인 실패 처리가 가능합니다." }
        this.status = ReservationStatus.FAILED_RENTER_ISSUE
    }

    // 예약 요청 취소
    fun canceled() {
        check(this.status == ReservationStatus.REQUESTED) { "요청 대기 상태에서만 취소 처리가 가능합니다." }
        this.status = ReservationStatus.CANCELED
    }
}