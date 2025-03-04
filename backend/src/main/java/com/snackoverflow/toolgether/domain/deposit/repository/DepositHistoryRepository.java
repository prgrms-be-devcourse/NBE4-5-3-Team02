package com.snackoverflow.toolgether.domain.deposit.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory;

public interface DepositHistoryRepository extends JpaRepository<DepositHistory, Long> {
	Optional<DepositHistory> findByReservationId(Long reservationId);
}
