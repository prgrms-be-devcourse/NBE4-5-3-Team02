package com.snackoverflow.toolgether.domain.Deposit.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snackoverflow.toolgether.domain.Deposit.entity.DepositHistory;
import com.snackoverflow.toolgether.domain.Deposit.repository.DepositRepository;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DepositService {

	private final DepositRepository depositRepository;

	@Transactional
	public DepositHistory createDepositHistory(DepositHistory depositHistory) {
		return depositRepository.save(depositHistory);
	}
}
