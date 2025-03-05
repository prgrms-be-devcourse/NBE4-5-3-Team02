package com.snackoverflow.toolgether.domain.reservation.repository;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
}
