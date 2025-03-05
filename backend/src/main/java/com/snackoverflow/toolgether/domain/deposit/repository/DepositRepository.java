package com.snackoverflow.toolgether.domain.deposit.repository;

import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DepositRepository extends JpaRepository<DepositHistory, Long> {
}
