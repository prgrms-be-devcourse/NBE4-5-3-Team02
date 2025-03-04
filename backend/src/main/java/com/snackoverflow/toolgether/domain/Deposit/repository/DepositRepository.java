package com.snackoverflow.toolgether.domain.Deposit.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.snackoverflow.toolgether.domain.Deposit.entity.DepositHistory;

public interface DepositRepository extends JpaRepository<DepositHistory, Long> {
}
