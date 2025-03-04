package com.snackoverflow.toolgether.domain.reservation.service;

import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.snackoverflow.toolgether.domain.ReturnReason;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositStatus;
import com.snackoverflow.toolgether.domain.deposit.service.DepositHistoryService;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.repository.PostRepository;
import com.snackoverflow.toolgether.domain.post.service.PostService;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.user.repository.UserRepository;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationRequest;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationResponse;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository;
import com.snackoverflow.toolgether.domain.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservationService {
	private final ReservationRepository reservationRepository;
	private final PostService postService;
	private final UserService userService;
	private final DepositHistoryService depositHistoryService;

	// 예약 요청
	@Transactional
	public ReservationResponse requestReservation(ReservationRequest reservationRequest) {
		Post post = postService.findPostById(reservationRequest.postId());
		User renter = userService.findUserById(reservationRequest.renterId());
		User owner = userService.findUserById(reservationRequest.ownerId());

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

	// 예약 승인
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

		depositHistoryService.createDepositHistory(depositHistory);
	}

	// 예약 거절
	@Transactional
	public void rejectReservation(Long reservationId, String reason) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new RuntimeException("Reservation not found"));
		reservation.reject(reason);
	}

	// 대여 시작 (IN_PROGRESS 상태)
	@Transactional
	public void startRental(Long reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new RuntimeException("Reservation not found"));
		reservation.startRental();
	}

	// 대여 완료 (DONE 상태)
	@Transactional
	public void completeRental(Long reservationId) {
		Reservation reservation = reservationRepository.findById(reservationId)
			.orElseThrow(() -> new RuntimeException("Reservation not found"));

		User renter = userService.findUserById(reservation.getRenter().getId());

		reservation.completeRental();

		// 보증금 상태 변경 및 반환 사유 업데이트
		DepositHistory depositHistory = depositHistoryService.findDepositHistoryByReservationId(reservationId);
		depositHistoryService.updateDepositHistory(depositHistory.getId(), DepositStatus.RETURNED, ReturnReason.NORMAL_COMPLETION);

		// 사용자 크레딧 업데이트
		userService.updateUserCredit(renter.getId(), depositHistory.getAmount());
	}
}
