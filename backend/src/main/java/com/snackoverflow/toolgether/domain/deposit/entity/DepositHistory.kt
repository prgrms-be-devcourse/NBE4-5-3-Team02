package com.snackoverflow.toolgether.domain.deposit.entity

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation
import com.snackoverflow.toolgether.domain.user.entity.User
import jakarta.persistence.*

@Entity
class DepositHistory {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    lateinit var reservation: Reservation

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    lateinit var user: User

    @Column(nullable = false)
    var amount: Int = 0

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    lateinit var status: DepositStatus

    @Enumerated(EnumType.STRING)
    lateinit var returnReason: ReturnReason

    fun changeStatus(status: DepositStatus) {
        this.status = status
    }

    fun changeReturnReason(reason: ReturnReason) {
        this.returnReason = reason
    }
}