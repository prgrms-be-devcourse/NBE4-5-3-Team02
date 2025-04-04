package com.snackoverflow.toolgether.domain.reservation.service;

import com.snackoverflow.toolgether.domain.Notification.NotificationService;
import com.snackoverflow.toolgether.domain.post.repository.PostRepository;
import com.snackoverflow.toolgether.domain.reservation.controller.SseController;
import com.snackoverflow.toolgether.domain.reservation.dto.PostReservationResponse;
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus;
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.Trigger;
import org.quartz.TriggerBuilder;
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
import com.snackoverflow.toolgether.domain.user.service.UserService;
import com.snackoverflow.toolgether.global.exception.custom.ErrorResponse;
import com.snackoverflow.toolgether.global.exception.custom.CustomException;


@Slf4j
@Service
@RequiredArgsConstructor
public class ReservationService {
	private final ReservationRepository reservationRepository;
	private final PostRepository postRepository;
	private final PostService postService;
	private final UserService userService;
	private final DepositHistoryService depositHistoryService;
	private final NotificationService notificationService;

	// Quartz
	private final Scheduler scheduler; // Quartz 스케줄러
	private final JobDetail startRentalJobDetail; // Quartz JobDetail (시작)
	private final JobDetail completeRentalJobDetail; // Quartz JobDetail (종료)

	@PostConstruct
	public void init() {
		try {
			if (!scheduler.isStarted()) { // 이미 시작되었는지 확인, 불필요한 재시작 방지
				scheduler.start();
				log.info("Quartz Scheduler started.");
			}
		} catch (SchedulerException e) {
			log.error("Failed to start Quartz Scheduler", e);
			// throw new RuntimeException("Failed to start Quartz Scheduler", e); // 또는 다른 예외 처리
			// 시작 실패 시, 애플리케이션을 중단시키는 것이 좋을 수도 있음
		}
	}

	// 예약 요청 (일정 충돌 방지 로직 필요)
	@Transactional
	public ReservationResponse requestReservation(ReservationRequest reservationRequest) {
		// 1. Post, Renter, Owner 조회
		Post post = postService.findPostById(reservationRequest.postId());
		User renter = userService.findUserById(reservationRequest.renterId());
		User owner = userService.findUserById(reservationRequest.ownerId());

		// 2. 일정 충돌 검증 (비관적 락 적용)
		List<Reservation> conflictingReservations = reservationRepository.findConflictingReservations(
				reservationRequest.postId(),
				reservationRequest.startTime(),
				reservationRequest.endTime());

		if (!conflictingReservations.isEmpty()) {
			throw new IllegalArgumentException("해당 시간대에는 이미 예약이 존재합니다.");
		}

		// 3. 예약 생성 및 저장
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

		// 4. DepositHistory 생성 및 저장
		DepositHistory depositHistory = DepositHistory.builder()
			.reservation(reservation)
			.user(reservation.getRenter()) // 보증금은 대여자가 지불
			.amount(reservationRequest.deposit().intValue())
			.status(DepositStatus.PENDING)
			.returnReason(ReturnReason.NONE)
			.build();

		depositHistoryService.createDepositHistory(depositHistory);

		// 알림 전송 (소유자에게 알림)
		notificationService.createNotification(reservation.getOwner().getId(), "[%d] '%s' 새로운 예약 요청이 있습니다.".formatted(reservation.getId(), reservation.getPost().getTitle()));

		// 5. Response 반환
		return new ReservationResponse(reservation.getId(),
			reservation.getStatus().name(),
			reservation.getPost().getId(),
			reservation.getStartTime(),
			reservation.getEndTime(),
			reservation.getAmount(),
			reservation.getRejectionReason(),
			reservation.getOwner().getId(),
			reservation.getRenter().getId()
		);
	}

	// 예약 승인
	@Transactional
	public void approveReservation(Long reservationId) {
		try {
			Reservation reservation = findReservationByIdOrThrow(reservationId);
			reservation.approve();

			// 알림 전송 (대여자에게 알림)
			notificationService.createNotification(reservation.getRenter().getId(), "[%d] '%s' 예약이 승인되었습니다.".formatted(reservationId, reservation.getPost().getTitle()));

			// Quartz Job 등록 (Start Rental)
			JobDataMap startJobDataMap = new JobDataMap();
			startJobDataMap.put("reservationId", reservationId); // 확인: reservationId가 null이 아닌지

			// JobDetail에 JobDataMap 설정 (여기 수정)
			JobDetail startJob = startRentalJobDetail.getJobBuilder()
				.usingJobData(startJobDataMap) // usingJobData 사용
				.withIdentity("startRentalJob-" + reservationId, "startRentalGroup")
				.build();

			Trigger startTrigger = TriggerBuilder.newTrigger()
				.forJob(startJob) // 수정: startJob 사용
				.withIdentity("startRentalTrigger-" + reservationId, "startRentalGroup") // 그룹 지정
				.startAt(
					Date.from(reservation.getStartTime().atZone(ZoneId.systemDefault()).toInstant())) // startTime에 실행
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
				.build();

			// Quartz Job 등록 (Complete Rental)
			JobDataMap completeJobDataMap = new JobDataMap();
			completeJobDataMap.put("reservationId", reservationId); //확인

			// JobDetail에 JobDataMap 설정
			JobDetail completeJob = completeRentalJobDetail.getJobBuilder()
				.usingJobData(completeJobDataMap) // usingJobData 사용
				.withIdentity("completeRentalJob-" + reservationId, "completeRentalGroup") // 더 구체적인 이름, 그룹
				.build();

			Trigger completeTrigger = TriggerBuilder.newTrigger()
				.forJob(completeJob) // 수정: completeJob 사용
				.withIdentity("completeRentalTrigger-" + reservationId, "completeRentalGroup") // 그룹 지정
				.startAt(Date.from(reservation.getEndTime().atZone(ZoneId.systemDefault()).toInstant()))// endTime에 실행
				.withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
				.build();

			System.out.println("대여 시작 시간 : " + startTrigger.getStartTime());
			System.out.println("대여 종료 시간 : " + completeTrigger.getStartTime());

			log.info("Scheduling StartRentalJob. Job Key: {}, Trigger Key: {}", startJob.getKey(), startTrigger.getKey()); // Job/Trigger 정보
			log.info("Scheduling CompleteRentalJob. Job Key: {}, Trigger Key: {}", completeJob.getKey(), completeTrigger.getKey()); // Job/Trigger 정보

			scheduler.scheduleJob(startJob, Set.of(startTrigger), true);
			scheduler.scheduleJob(completeJob, Set.of(completeTrigger), true);

			log.info("Job scheduled.  StartJob exists: {}, CompleteJob exists: {}",
				scheduler.checkExists(startJob.getKey()), scheduler.checkExists(completeJob.getKey()));
			log.info("Trigger scheduled. StartTrigger exists: {}, CompleteTrigger exists: {}",
				scheduler.checkExists(startTrigger.getKey()), scheduler.checkExists(completeTrigger.getKey()));

		}  catch (SchedulerException e) {
			// SchedulerException을 RuntimeException으로 래핑하여 던짐
			throw new RuntimeException("Failed to schedule start/complete rental job", e);
		}
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

		String title = reservation.getPost().getTitle();

		// 알림 전송 (대여자에게 알림)
		notificationService.createNotification(reservation.getRenter().getId(), "[%d] '%s' 예약이 거절되었습니다.".formatted(reservationId, reservation.getPost().getTitle()));
	}

	// 대여 시작 (IN_PROGRESS 상태)
	@Transactional
	public void startRental(Long reservationId) {
		Reservation reservation = findReservationByIdOrThrow(reservationId);
		reservation.startRental();

		// 알림 전송
		notificationService.createNotification(reservation.getRenter().getId(), "[%d] '%s' 대여가 시작되었습니다.".formatted(reservationId, reservation.getPost().getTitle()));
		notificationService.createNotification(reservation.getOwner().getId(), "[%d] '%s' 대여가 시작되었습니다.".formatted(reservationId, reservation.getPost().getTitle()));
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

		// 알림 전송
		notificationService.createNotification(reservation.getRenter().getId(), "[%d] '%s' 대여가 완료되었습니다.".formatted(reservationId, reservation.getPost().getTitle()));
		notificationService.createNotification(reservation.getOwner().getId(), "[%d] '%s' 대여가 완료되었습니다.".formatted(reservationId, reservation.getPost().getTitle()));
	}

	// 대여자 예약 취소
	@Transactional
	public void cancelReservation(Long reservationId) {
		Reservation reservation = findReservationByIdOrThrow(reservationId);
		reservation.canceled();

		// 보증금 상태 변경 및 반환 사유 업데이트
		DepositHistory depositHistory = depositHistoryService.findDepositHistoryByReservationId(reservationId);
		depositHistoryService.updateDepositHistory(depositHistory.getId(), DepositStatus.RETURNED, ReturnReason.REJECTED);

		// 대여자 크레딧 업데이트
		userService.updateUserCredit(reservation.getRenter().getId(), depositHistory.getAmount());

		// 소유자에게 알림 전송
		notificationService.createNotification(reservation.getOwner().getId(), "[%d] '%s' 예약이 취소되었습니다.".formatted(reservationId, reservation.getPost().getTitle()));
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

		// 알림 전송
		notificationService.createNotification(reservation.getRenter().getId(), "[%d] '%s' 대여가 실패했습니다.".formatted(reservationId, reservation.getPost().getTitle()));
		notificationService.createNotification(reservation.getOwner().getId(), "'%s' 대여가 실패했습니다.".formatted(reservationId, reservation.getPost().getTitle()));
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
			reservation.getAmount(),
			reservation.getRejectionReason(),
			reservation.getOwner().getId(),
			reservation.getRenter().getId()
		);
	}

	// 예외 처리 포함 예약 조회 메서드
	public Reservation findReservationByIdOrThrow(Long reservationId) {
		return reservationRepository.findById(reservationId)
			.orElseThrow(() -> new CustomException(ErrorResponse.builder()
				.title("예약 조회 실패")
				.status(404)
				.detail("해당 ID의 예약을 찾을 수 없습니다.")
				.instance(URI.create(ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString()))
				.build()));
	}

	@Transactional(readOnly = true)
	public List<ReservationResponse> getReservationsByPostId(Long postId) {
		List<Reservation> reservations = reservationRepository.findByPostId(postId);
		List<ReservationResponse> responses = new ArrayList<>();
		List<ReservationStatus> includedStatuses = List.of(
			ReservationStatus.APPROVED,
			ReservationStatus.IN_PROGRESS
		);
		reservations.stream()
			.filter(reservation -> includedStatuses.contains(reservation.getStatus()))
			.forEach(reservation -> {
			responses.add(new ReservationResponse(
				reservation.getId(),
				reservation.getStatus().name(),
				reservation.getPost().getId(),
				reservation.getStartTime(),
				reservation.getEndTime(),
				reservation.getAmount(),
				reservation.getRejectionReason(),
				reservation.getOwner().getId(),
				reservation.getRenter().getId()
			));
		});
		return responses;
	}

	// 예약용 Post 조회 추가
	@Transactional(readOnly = true)
	public PostReservationResponse getPostById(Long postId) {
		Optional<Post> post = postRepository.findById(postId);
		return new PostReservationResponse(
			post.get().getId(),
			post.get().getUser().getId(),
			post.get().getTitle(),
			post.get().getPriceType().name(),
			post.get().getPrice()
		);
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

	@Transactional(readOnly = true)
	public Optional<Reservation> getReservationByIdForReview(Long reservationId) {
		return reservationRepository.findById(reservationId);
	}
}
