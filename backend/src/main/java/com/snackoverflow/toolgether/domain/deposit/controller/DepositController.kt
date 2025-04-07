package com.snackoverflow.toolgether.domain.deposit.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.snackoverflow.toolgether.domain.deposit.dto.DepositResponse;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory;
import com.snackoverflow.toolgether.domain.deposit.service.DepositHistoryService;
import com.snackoverflow.toolgether.global.dto.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/deposits")
@RequiredArgsConstructor
public class DepositController {

	private final DepositHistoryService depositService;

	@GetMapping("/rid/{id}")
	public RsData<DepositResponse> findDepositHistoryByReservationId(@PathVariable Long id) {
		DepositHistory depositHistory = depositService.findDepositHistoryByReservationId(id);
		return new RsData<>(
			"200-1",
			"%d번 예약의 보증금 내역이 조회되었습니다.".formatted(id),
			new DepositResponse(
				depositHistory.getId(),
				depositHistory.getStatus().toString(),
				depositHistory.getReservation().getId(),
				depositHistory.getReturnReason().toString(),
				depositHistory.getAmount()
				)
		);
	}
}
