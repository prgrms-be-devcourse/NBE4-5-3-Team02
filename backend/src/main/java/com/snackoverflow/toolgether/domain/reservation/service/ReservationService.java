package com.snackoverflow.toolgether.domain.reservation.service;

import org.springframework.stereotype.Service;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
	private final ReservationRepository reservationRepository;

	public Reservation save(Reservation reservation) {

	}
}
