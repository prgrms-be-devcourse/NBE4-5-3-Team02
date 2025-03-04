package com.snackoverflow.toolgether.domain.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
