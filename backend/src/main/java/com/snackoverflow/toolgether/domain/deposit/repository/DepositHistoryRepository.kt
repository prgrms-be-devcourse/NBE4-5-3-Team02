package com.snackoverflow.toolgether.domain.deposit.repository

import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface DepositHistoryRepository : JpaRepository<DepositHistory?, Long?> {
    fun findByReservationId(reservationId: Long): Optional<DepositHistory>
}