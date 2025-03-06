package com.snackoverflow.toolgether.domain.reservation.repository;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // renterId를 기준으로 예약 정보를 조회하는 메서드
    List<Reservation> findByRenterId(Long renterId);

    // ownerId를 기준으로 예약 정보를 조회하는 메서드
    List<Reservation> findByOwnerId(Long ownerId);
}
