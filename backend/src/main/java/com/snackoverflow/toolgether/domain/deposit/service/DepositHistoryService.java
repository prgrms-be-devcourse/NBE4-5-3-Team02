package com.snackoverflow.toolgether.domain.deposit.service;

import java.net.URI;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.snackoverflow.toolgether.domain.deposit.entity.ReturnReason;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositStatus;
import com.snackoverflow.toolgether.domain.deposit.repository.DepositHistoryRepository;
import com.snackoverflow.toolgether.global.exception.custom.CustomException;
import com.snackoverflow.toolgether.global.exception.custom.ErrorResponse;

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
		DepositHistory depositHistory = findDepositHistoryById(depositHistoryId);
		depositHistory.changeStatus(status);
		depositHistory.changeReturnReason(returnReason);
		return depositHistory;
	}

	@Transactional
	public DepositHistory findDepositHistoryByReservationId(Long reservationId) {
		return depositHistoryRepository.findByReservationId(reservationId)
			.orElseThrow(() -> new RuntimeException("DepositHistory not found"));
	}

	public DepositHistory findDepositHistoryById(Long depositHistoryId) {
		return depositHistoryRepository.findById(depositHistoryId)
			.orElseThrow(() -> new CustomException(ErrorResponse.builder()
				.title("보증금 내역 조회 실패")
				.status(404)
				.detail("해당 ID의 보증금 내역을 찾을 수 없습니다.")
				.instance(URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString()))
				.build()));
	}
}
