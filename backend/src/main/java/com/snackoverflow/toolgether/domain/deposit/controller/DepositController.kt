package com.snackoverflow.toolgether.domain.deposit.controller

import com.snackoverflow.toolgether.domain.deposit.dto.DepositResponse
import com.snackoverflow.toolgether.domain.deposit.service.DepositHistoryService
import com.snackoverflow.toolgether.global.dto.RsData
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/deposits")
class DepositController(
    private val depositService: DepositHistoryService
) {

    @GetMapping("/rid/{id}")
    fun findDepositHistoryByReservationId(@PathVariable id: Long): RsData<DepositResponse> {
        val depositHistory = depositService.findDepositHistoryByReservationId(id)
        return RsData(
            "200-1",
            "${id}번 예약의 보증금 내역이 조회되었습니다.",
            DepositResponse(
                depositHistory.id!!,
                depositHistory.status.toString(),
                depositHistory.reservation.id!!,
                depositHistory.returnReason.toString(),
                depositHistory.amount
            )
        )
    }
}