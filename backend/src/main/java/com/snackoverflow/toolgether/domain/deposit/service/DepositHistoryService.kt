package com.snackoverflow.toolgether.domain.deposit.service

import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory
import com.snackoverflow.toolgether.domain.deposit.entity.DepositStatus
import com.snackoverflow.toolgether.domain.deposit.entity.ReturnReason
import com.snackoverflow.toolgether.domain.deposit.repository.DepositHistoryRepository
import com.snackoverflow.toolgether.global.exception.custom.CustomException
import com.snackoverflow.toolgether.global.exception.custom.ErrorResponse
import lombok.RequiredArgsConstructor
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI

@Service
@RequiredArgsConstructor
class DepositHistoryService(
    private val depositHistoryRepository: DepositHistoryRepository
) {
    @Transactional
    fun createDepositHistory(depositHistory: DepositHistory): DepositHistory {
        return depositHistoryRepository.save(depositHistory)
    }

    @Transactional
    fun updateDepositHistory(
        depositHistoryId: Long,
        status: DepositStatus,
        returnReason: ReturnReason
    ): DepositHistory? {
        val depositHistory: DepositHistory? = findDepositHistoryById(depositHistoryId)
        depositHistory?.changeStatus(status)
        depositHistory?.changeReturnReason(returnReason)
        return depositHistory
    }

    @Transactional(readOnly = true)
    fun findDepositHistoryByReservationId(reservationId: Long): DepositHistory {
        return depositHistoryRepository.findByReservationId(reservationId)
            .orElseThrow {
                CustomException(
                    ErrorResponse.builder()
                        .title("예약 ID 기반 보증금 내역 조회 실패")
                        .status(404)
                        .detail("해당 예약 ID의 보증금 내역을 찾을 수 없습니다.")
                        .instance(URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString()))
                        .build()
                )
            }
    }

    @Transactional(readOnly = true)
    fun findDepositHistoryById(depositHistoryId: Long): DepositHistory? {
        return depositHistoryRepository.findById(depositHistoryId)
            .orElseThrow {
                CustomException(
                    ErrorResponse.builder()
                        .title("보증금 내역 조회 실패")
                        .status(404)
                        .detail("해당 ID의 보증금 내역을 찾을 수 없습니다.")
                        .instance(URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString()))
                        .build()
                )
            }
    }
}