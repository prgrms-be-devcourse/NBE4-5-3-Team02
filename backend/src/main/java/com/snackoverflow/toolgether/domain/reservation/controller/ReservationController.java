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
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/reservations")
@RequiredArgsConstructor
public class ReservationController {
	private final ReservationService reservationService;

	@PostMapping("/request")
	public ReservationResponse createReservation(@RequestBody ReservationRequest reservationRequest) {
		return reservationService.requestReservation(reservationRequest);
	}

	@PostMapping("/{id}/approve")
	public void approveReservation(@PathVariable Long id) {
		reservationService.approveReservation(id);
	}

	@PatchMapping("/{id}/reject")
	public void rejectReservation(@PathVariable Long id, @RequestParam String reason) {
		reservationService.rejectReservation(id, reason);
	}

	@PatchMapping("/{id}/start")
	public void startRental(@PathVariable Long id) {
		reservationService.startRental(id);
	}

	@PatchMapping("/{id}/complete")
	public void completeRental(@PathVariable Long id) {
		reservationService.completeRental(id);
	}
}
