package com.snackoverflow.toolgether.domain.deposit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snackoverflow.toolgether.domain.ReturnReason;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositStatus;
import com.snackoverflow.toolgether.domain.deposit.repository.DepositHistoryRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositHistoryService {

	private final DepositHistoryRepository depositHistoryRepository;

	@Transactional
	public DepositHistory createDepositHistory(DepositHistory depositHistory) {
		return depositHistoryRepository.save(depositHistory);
	}

	@Transactional
	public DepositHistory updateDepositHistory(Long depositHistoryId, DepositStatus status, ReturnReason returnReason) {
		DepositHistory depositHistory = depositHistoryRepository.findById(depositHistoryId)
			.orElseThrow(() -> new RuntimeException("DepositHistory not found"));
		depositHistory.changeStatus(status);
		depositHistory.changeReturnReason(returnReason);
		return depositHistory;
	}

	@Transactional
	public DepositHistory findDepositHistoryByReservationId(Long reservationId) {
		return depositHistoryRepository.findByReservationId(reservationId)
			.orElseThrow(() -> new RuntimeException("DepositHistory not found"));
	}
}
