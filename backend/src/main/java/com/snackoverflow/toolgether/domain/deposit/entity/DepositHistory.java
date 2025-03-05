package com.snackoverflow.toolgether.domain.deposit.entity;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.user.entity.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DepositHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private int amount; // 보증금 금액

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DepositStatus status; // 보증금 상태 (PENDING, RETURNED)

    @Enumerated(EnumType.STRING)
    private ReturnReason returnReason; // 보증금 반환 사유

    public void changeStatus(DepositStatus status) {
        this.status = status;
    }

    public void changeReturnReason(ReturnReason returnReason) {
        this.returnReason = returnReason;
    }
}
