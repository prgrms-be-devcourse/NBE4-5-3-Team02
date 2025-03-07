package com.snackoverflow.toolgether.domain.reservation.service;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository;
import lombok.RequiredArgsConstructor;
import java.net.URI;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.snackoverflow.toolgether.domain.deposit.entity.ReturnReason;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory;
import com.snackoverflow.toolgether.domain.deposit.entity.DepositStatus;
import com.snackoverflow.toolgether.domain.deposit.service.DepositHistoryService;
import com.snackoverflow.toolgether.domain.post.entity.Post;
import com.snackoverflow.toolgether.domain.post.service.PostService;
import com.snackoverflow.toolgether.domain.reservation.entity.FailDue;
import com.snackoverflow.toolgether.domain.user.entity.User;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationRequest;
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationResponse;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository;
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.exception.custom.ErrorResponse;
import com.snackoverflow.toolgether.global.exception.custom.CustomException;

import lombok.RequiredArgsConstructor;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReservationService {
	private final ReservationRepository reservationRepository;
	private final PostService postService;
	private final UserService userService;
	private final DepositHistoryService depositHistoryService;

	// 예약 요청 (일정 충돌 방지 로직 필요)
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

		// 예약 요청 시 보증금 결제 -> DepositHistory 추가
		DepositHistory depositHistory = DepositHistory.builder()
			.reservation(reservation)
			.user(reservation.getRenter()) // 보증금은 대여자가 지불
			.amount(reservationRequest.deposit().intValue())
			.status(DepositStatus.PENDING)
			.returnReason(ReturnReason.NONE)
			.build();

		depositHistoryService.createDepositHistory(depositHistory);
		return new ReservationResponse(reservation.getId(), reservation.getStatus().name(), reservation.getPost().getId(), reservation.getStartTime(), reservation.getEndTime(), reservation.getAmount());
	}

	// 예약 승인
	@Transactional
	public void approveReservation(Long reservationId) {
		Reservation reservation = findReservationByIdOrThrow(reservationId);
		reservation.approve();
	}

	// 예약 거절
	@Transactional
	public void rejectReservation(Long reservationId, String reason) {
		Reservation reservation = findReservationByIdOrThrow(reservationId);
		reservation.reject(reason);

		// 보증금 상태 변경 및 반환 사유 업데이트
		DepositHistory depositHistory = depositHistoryService.findDepositHistoryByReservationId(reservationId);
		depositHistoryService.updateDepositHistory(depositHistory.getId(), DepositStatus.RETURNED, ReturnReason.REJECTED);

		// 대여자 크레딧 업데이트
		userService.updateUserCredit(reservation.getRenter().getId(), depositHistory.getAmount());
	}

	// 대여 시작 (IN_PROGRESS 상태)
	@Transactional
	public void startRental(Long reservationId) {
		Reservation reservation = findReservationByIdOrThrow(reservationId);
		reservation.startRental();
	}

	// 대여 완료 (DONE 상태)
	@Transactional
	public void completeRental(Long reservationId) {
		Reservation reservation = findReservationByIdOrThrow(reservationId);
		reservation.completeRental();

		// 보증금 상태 변경 및 반환 사유 업데이트
		DepositHistory depositHistory = depositHistoryService.findDepositHistoryByReservationId(reservationId);
		depositHistoryService.updateDepositHistory(depositHistory.getId(), DepositStatus.RETURNED, ReturnReason.NORMAL_COMPLETION);

		// 대여자 크레딧 업데이트
		userService.updateUserCredit(reservation.getRenter().getId(), depositHistory.getAmount());
	}

	// ~에 의한 대여 실패 -> 소유자의 경우 대여자에게, 대여자일 경우 소유자에게 보증금 환급
	@Transactional
	public void failDueTo(Long reservationId, String reason, FailDue failDue) {
		Reservation reservation = findReservationByIdOrThrow(reservationId);

		// 보증금 상태 변경 및 반환 사유 업데이트 -> 사유를 받아와서 작성
		DepositHistory depositHistory = depositHistoryService.findDepositHistoryByReservationId(reservationId);
		depositHistoryService.updateDepositHistory(depositHistory.getId(), DepositStatus.RETURNED, ReturnReason.valueOf(reason));

		if(failDue.equals(FailDue.OWNER_ISSUE)){
			reservation.failDueToOwnerIssue();
			// 대여자 크레딧 업데이트
			userService.updateUserCredit(reservation.getRenter().getId(), depositHistory.getAmount());
		}
		else if(failDue.equals(FailDue.RENTER_ISSUE)){
			reservation.failDueToRenterIssue();
			// 소유자 크레딧 업데이트
			userService.updateUserCredit(reservation.getOwner().getId(), depositHistory.getAmount());
		}
	}

	// 예약 ID로 예약 조회
	public ReservationResponse getReservationById(Long reservationId) {
		Reservation reservation = findReservationByIdOrThrow(reservationId);
		return new ReservationResponse(
			reservation.getId(),
			reservation.getStatus().name(),
			reservation.getPost().getId(),
			reservation.getStartTime(),
			reservation.getEndTime(),
			reservation.getAmount()
		);
	}

	// 예외 처리 포함 예약 조회 메서드
	private Reservation findReservationByIdOrThrow(Long reservationId) {
		return reservationRepository.findById(reservationId)
			.orElseThrow(() -> new CustomException(ErrorResponse.builder()
				.title("예약 조회 실패")
				.status(404)
				.detail("해당 ID의 예약을 찾을 수 없습니다.")
				.instance(URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString()))
				.build()));
	}

    // 렌탈 예약 정보 DB에서 조회
    @Transactional(readOnly = true)
    public List<Reservation> getRentalReservations(Long userId) {
        return reservationRepository.findByOwnerId(userId);
    }

    // 대여 예약 정보 DB에서 조회
    @Transactional(readOnly = true)
    public List<Reservation> getBorrowReservations(Long userId) {
        return reservationRepository.findByRenterId(userId);
    }
}
