package com.snackoverflow.toolgether.domain.reservation.controller;

import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.snackoverflow.toolgether.domain.reservation.dto.ReservationRequest;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationResponse;
import com.snackoverflow.toolgether.domain.reservation.entity.FailDue;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;
import com.snackoverflow.toolgether.global.dto.RsData;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
	private final ReservationService reservationService;

	@PostMapping("/request")
	public RsData<ReservationResponse> createReservation(@RequestBody ReservationRequest reservationRequest) {
		ReservationResponse response = reservationService.requestReservation(reservationRequest);
		return new RsData<>("201-1", "예약 요청 성공", response);
	}

	// 예약 상태를 승인으로 바꾼 후 DepositHistory 생성
	@PatchMapping("/{id}/approve")
	public RsData<Void> approveReservation(@PathVariable Long id) {
		reservationService.approveReservation(id);
		return new RsData<>("201-1",
			"%d번 예약 승인 성공".formatted(id));
	}

	@PatchMapping("/{id}/reject")
	public RsData<Void> rejectReservation(@PathVariable Long id, @RequestParam String reason) {
		reservationService.rejectReservation(id, reason);
		return new RsData<>("200-1",
			"%d번 예약 거절 성공".formatted(id));
	}

	@PatchMapping("/{id}/start")
	public RsData<Void> startRental(@PathVariable Long id) {
		reservationService.startRental(id);
		return new RsData<>("200-1",
			"%d번 예약 대여 시작 성공".formatted(id));
	}

	@PatchMapping("/{id}/complete")
	public RsData<Void> completeRental(@PathVariable Long id) {
		reservationService.completeRental(id);
		return new RsData<>("200-1",
			"%d번 예약 대여 종료 성공".formatted(id));
	}

	@PatchMapping("/{id}/ownerIssue")
	public RsData<Void> ownerIssue(@PathVariable Long id, @RequestParam String reason) {
		reservationService.failDueTo(id, reason, FailDue.OWNER_ISSUE);
		return new RsData<>("200-1",
			"%d번 예약 소유자에 의한 이슈로 환급 성공".formatted(id));
	}

	@PatchMapping("/{id}/renterIssue")
	public RsData<Void> renterIssue(@PathVariable Long id, @RequestParam String reason) {
		reservationService.failDueTo(id, reason, FailDue.RENTER_ISSUE);
		return new RsData<>("200-1",
			"%d번 예약 대여자에 의한 이슈로 환급 성공".formatted(id));
	}
}
