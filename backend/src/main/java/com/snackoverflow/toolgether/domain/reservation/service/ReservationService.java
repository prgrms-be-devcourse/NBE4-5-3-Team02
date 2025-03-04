package com.snackoverflow.toolgether.domain.reservation.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snackoverflow.toolgether.domain.Deposit.entity.DepositHistory;
import com.snackoverflow.toolgether.domain.Deposit.entity.DepositStatus;
import com.snackoverflow.toolgether.domain.Deposit.repository.DepositRepository;
import com.snackoverflow.toolgether.domain.Deposit.service.DepositService;
import com.snackoverflow.toolgether.domain.Post.entity.Post;
import com.snackoverflow.toolgether.domain.Post.repository.PostRepository;
import com.snackoverflow.toolgether.domain.User.entity.User;
import com.snackoverflow.toolgether.domain.User.repository.UserRepository;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationRequest;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationResponse;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
	private final ReservationRepository reservationRepository;
	private final PostRepository postRepository;
	private final UserRepository userRepository;
	private final DepositService depositService;

	@Transactional
	public ReservationResponse requestReservation(ReservationRequest reservationRequest) {
		Post post = postRepository.findById(reservationRequest.postId())
			.orElseThrow(() -> new RuntimeException("Post not found"));
		User renter = userRepository.findById(reservationRequest.renterId())
			.orElseThrow(() -> new RuntimeException("Renter not found"));
		User owner = userRepository.findById(reservationRequest.ownerId())
			.orElseThrow(() -> new RuntimeException("Owner not found"));

		Double totalAmount = reservationRequest.deposit() + reservationRequest.rentalFee();
		Reservation reservation = Reservation.builder()
			.post(post)
			.renter(renter)
			.owner(owner)
			.startTime(reservationRequest.startTime())
			.endTime(reservationRequest.endTime())
			.amount(totalAmount)
			.status(ReservationStatus.REQUESTED)
			.createAt(LocalDateTime.now())
			.build();

		reservationRepository.save(reservation);
		return new ReservationResponse(reservation.getId(), reservation.getStatus().name(), reservation.getAmount());
	}

	@Transactional
	public void approveReservation(Long reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new RuntimeException("Reservation not found"));
		reservation.approve();

		// 예약 승인 시 보증금 결제 -> DepositHistory 추가
		DepositHistory depositHistory = DepositHistory.builder()
			.reservation(reservation)
			.user(reservation.getRenter()) // 보증금은 대여자(renter)가 지불
			.amount(reservation.getAmount().intValue())
			.status(DepositStatus.PENDING)
			.build();

		depositService.createDepositHistory(depositHistory);
	}

	@Transactional
	public void rejectReservation(Long reservationId, String reason) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new RuntimeException("Reservation not found"));
		reservation.reject(reason);
	}

}
