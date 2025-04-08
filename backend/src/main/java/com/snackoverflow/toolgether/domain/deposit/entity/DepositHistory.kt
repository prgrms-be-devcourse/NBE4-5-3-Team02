package com.snackoverflow.toolgether.domain.deposit.entity

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation
import com.snackoverflow.toolgether.domain.user.entity.User
import jakarta.persistence.*

@Entity
class DepositHistory(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    var reservation: Reservation,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User,

    @Column(nullable = false)
    var amount: Int = 0,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: DepositStatus,

    @Enumerated(EnumType.STRING)
    var returnReason: ReturnReason
) {
    constructor() : this(null, Reservation(), User(), 0, DepositStatus.PENDING, ReturnReason.NONE)
    constructor(reservation: Reservation, user: User, amount: Int, depositStatus: DepositStatus, returnReason: ReturnReason) : this() {
        this.reservation = reservation
        this.user = user
        this.amount = amount
        this.status = depositStatus
        this.returnReason = returnReason
    }

    fun changeStatus(status: DepositStatus) {
        this.status = status
    }

    fun changeReturnReason(reason: ReturnReason) {
        this.returnReason = reason
    }
}