package com.snackoverflow.toolgether.domain.reservation.service

import com.snackoverflow.toolgether.domain.notification.service.NotificationService
import com.snackoverflow.toolgether.domain.deposit.entity.DepositHistory
import com.snackoverflow.toolgether.domain.deposit.entity.DepositStatus
import com.snackoverflow.toolgether.domain.deposit.entity.ReturnReason
import com.snackoverflow.toolgether.domain.deposit.service.DepositHistoryService
import com.snackoverflow.toolgether.domain.post.repository.PostRepository
import com.snackoverflow.toolgether.domain.post.service.PostService
import com.snackoverflow.toolgether.domain.reservation.dto.PostReservationResponse
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationRequest
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationResponse
import com.snackoverflow.toolgether.domain.reservation.entity.FailDue
import com.snackoverflow.toolgether.domain.reservation.entity.Reservation
import com.snackoverflow.toolgether.domain.reservation.entity.ReservationStatus
import com.snackoverflow.toolgether.domain.reservation.repository.ReservationRepository
import com.snackoverflow.toolgether.domain.user.service.UserService
import com.snackoverflow.toolgether.global.exception.custom.CustomException
import com.snackoverflow.toolgether.global.exception.custom.ErrorResponse
import jakarta.annotation.PostConstruct
import lombok.extern.slf4j.Slf4j
import mu.KotlinLogging
import org.quartz.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.servlet.support.ServletUriComponentsBuilder
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*
import kotlin.collections.List
import kotlin.collections.MutableList

@Slf4j
@Service
class ReservationService(
    private val reservationRepository: ReservationRepository,
    private val postRepository: PostRepository,
    private val postService: PostService,
    private val userService: UserService,
    private val depositHistoryService: DepositHistoryService,
    private val notificationService: NotificationService,
    private val scheduler: Scheduler, // Quartz 스케줄러
    private val startRentalJobDetail: JobDetail, // Quartz JobDetail (시작)
    private val completeRentalJobDetail: JobDetail // Quartz JobDetail (종료)
) {
    private val log = KotlinLogging.logger {}

    @PostConstruct
    fun init() {
        try {
            if (!scheduler.isStarted) { // 이미 시작되었는지 확인, 불필요한 재시작 방지
                scheduler.start()
                log.info("Quartz Scheduler started.")
            }
        } catch (e: SchedulerException) {
            log.error("Failed to start Quartz Scheduler", e)
        }
    }

    // 예약 요청 (일정 충돌 방지 로직 필요)
    @Transactional
    fun requestReservation(reservationRequest: ReservationRequest): ReservationResponse {
        // 1. Post, Renter, Owner 조회
        val post = postService.findPostById(reservationRequest.postId)
        val renter = userService.findUserById(reservationRequest.renterId)
        val owner = userService.findUserById(reservationRequest.ownerId)

        // 2. 일정 충돌 검증 (비관적 락 적용)
        val conflictingReservations = reservationRepository.findConflictingReservations(
            reservationRequest.postId,
            reservationRequest.startTime,
            reservationRequest.endTime
        )

        if (conflictingReservations != null) {
            require(conflictingReservations.isEmpty()) { "해당 시간대에는 이미 예약이 존재합니다." }
        }

        // 3. 예약 생성 및 저장
        val totalAmount = reservationRequest.deposit + reservationRequest.rentalFee
        val reservation: Reservation = Reservation(
            post,
            renter,
            owner,
            LocalDateTime.now(),
            reservationRequest.startTime,
            reservationRequest.endTime,
            ReservationStatus.REQUESTED,
            totalAmount.toDouble()
            )

        reservationRepository.save(reservation)

        // 4. DepositHistory 생성 및 저장
        val depositHistory: DepositHistory = DepositHistory(
            reservation,
            reservation.renter,
            reservationRequest.deposit.toInt(),
            DepositStatus.PENDING,
            ReturnReason.NONE
        )

        depositHistoryService.createDepositHistory(depositHistory)

        // 알림 전송 (소유자에게 알림)
        notificationService.createNotification(
            reservation.owner.id!!,
            "[${reservation.id}] '${reservation.post.getTitle()}' 새로운 예약 요청이 있습니다."
        )

        // 5. Response 반환
        return ReservationResponse(
            reservation.id!!,
            reservation.status.name,
            reservation.post.id!!,
            reservation.startTime,
            reservation.endTime,
            reservation.amount,
            reservation.rejectionReason.toString(),
            reservation.owner.id!!,
            reservation.renter.id!!
        )
    }

    // 예약 승인
    @Transactional
    fun approveReservation(reservationId: Long) {
        try {
            val reservation = findReservationByIdOrThrow(reservationId)
            reservation.approve()

            // 알림 전송 (대여자에게 알림)
            notificationService.createNotification(
                reservation.renter.id,
                "[${reservation.id}] '${reservation.post.getTitle()}' 예약이 승인되었습니다."
            )

            // Quartz Job 등록 (Start Rental)
            val startJobDataMap = JobDataMap()
            startJobDataMap.put("reservationId", reservationId) // 확인: reservationId가 null이 아닌지

            // JobDetail에 JobDataMap 설정 (여기 수정)
            val startJob = startRentalJobDetail.jobBuilder
                .usingJobData(startJobDataMap) // usingJobData 사용
                .withIdentity("startRentalJob-$reservationId", "startRentalGroup")
                .build()

            val startTrigger: Trigger = TriggerBuilder.newTrigger()
                .forJob(startJob) // 수정: startJob 사용
                .withIdentity("startRentalTrigger-$reservationId", "startRentalGroup") // 그룹 지정
                .startAt(
                    Date.from(reservation.startTime.atZone(ZoneId.systemDefault()).toInstant())
                ) // startTime에 실행
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build()

            // Quartz Job 등록 (Complete Rental)
            val completeJobDataMap = JobDataMap()
            completeJobDataMap.put("reservationId", reservationId) //확인

            // JobDetail에 JobDataMap 설정
            val completeJob = completeRentalJobDetail.jobBuilder
                .usingJobData(completeJobDataMap) // usingJobData 사용
                .withIdentity("completeRentalJob-$reservationId", "completeRentalGroup") // 더 구체적인 이름, 그룹
                .build()

            val completeTrigger: Trigger = TriggerBuilder.newTrigger()
                .forJob(completeJob) // 수정: completeJob 사용
                .withIdentity("completeRentalTrigger-$reservationId", "completeRentalGroup") // 그룹 지정
                .startAt(Date.from(reservation.endTime.atZone(ZoneId.systemDefault()).toInstant())) // endTime에 실행
                .withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
                .build()

            scheduler.scheduleJob(startJob, mutableSetOf(startTrigger), true)
            scheduler.scheduleJob(completeJob, mutableSetOf(completeTrigger), true)

        } catch (e: SchedulerException) {
            // SchedulerException을 RuntimeException으로 래핑하여 던짐
            throw RuntimeException("Failed to schedule start/complete rental job", e)
        }
    }

    // 예약 거절
    @Transactional
    fun rejectReservation(reservationId: Long, reason: String?) {
        val reservation = findReservationByIdOrThrow(reservationId)
        reservation.reject(reason)

        // 보증금 상태 변경 및 반환 사유 업데이트
        val depositHistory: DepositHistory = depositHistoryService.findDepositHistoryByReservationId(reservationId)
        depositHistoryService.updateDepositHistory(depositHistory.id!!, DepositStatus.RETURNED, ReturnReason.REJECTED)

        // 대여자 크레딧 업데이트
        userService.updateUserCredit(reservation.renter.id, depositHistory.amount)

        val title = reservation.post.getTitle()

        // 알림 전송 (대여자에게 알림)
        notificationService.createNotification(
            reservation.renter.id,
            "[${reservation.id}] '${reservation.post.getTitle()}' 예약이 거절되었습니다."
        )
    }

    // 대여 시작 (IN_PROGRESS 상태)
    @Transactional
    fun startRental(reservationId: Long) {
        val reservation = findReservationByIdOrThrow(reservationId)
        reservation.startRental()

        // 알림 전송
        notificationService.createNotification(
            reservation.renter.id,
            "[${reservation.id}] '${reservation.post.getTitle()}' 대여가 시작되었습니다."
        )
        notificationService.createNotification(
            reservation.owner.id,
            "[${reservation.id}] '${reservation.post.getTitle()}' 대여가 시작되었습니다."
        )
    }

    // 대여 완료 (DONE 상태)
    @Transactional
    fun completeRental(reservationId: Long) {
        val reservation = findReservationByIdOrThrow(reservationId)
        reservation.completeRental()

        // 보증금 상태 변경 및 반환 사유 업데이트
        val depositHistory: DepositHistory = depositHistoryService.findDepositHistoryByReservationId(reservationId)
        depositHistoryService.updateDepositHistory(
            depositHistory.id!!,
            DepositStatus.RETURNED,
            ReturnReason.NORMAL_COMPLETION
        )

        // 대여자 크레딧 업데이트
        userService.updateUserCredit(reservation.renter.id, depositHistory.amount)

        // 알림 전송
        notificationService.createNotification(
            reservation.renter.id,
            "[${reservation.id}] '${reservation.post.getTitle()}' 대여가 완료되었습니다."
        )
        notificationService.createNotification(
            reservation.owner.id,
            "[${reservation.id}] '${reservation.post.getTitle()}' 대여가 완료되었습니다."
        )
    }

    // 대여자 예약 취소
    @Transactional
    fun cancelReservation(reservationId: Long) {
        val reservation = findReservationByIdOrThrow(reservationId)
        reservation.canceled()

        // 보증금 상태 변경 및 반환 사유 업데이트
        val depositHistory: DepositHistory = depositHistoryService.findDepositHistoryByReservationId(reservationId)
        depositHistoryService.updateDepositHistory(depositHistory.id!!, DepositStatus.RETURNED, ReturnReason.REJECTED)

        // 대여자 크레딧 업데이트
        userService.updateUserCredit(reservation.renter.id, depositHistory.amount)

        // 소유자에게 알림 전송
        notificationService.createNotification(
            reservation.owner.id,
            "[${reservation.id}] '${reservation.post.getTitle()}' 예약이 취소되었습니다."
        )
    }

    // ~에 의한 대여 실패 -> 소유자의 경우 대여자에게, 대여자일 경우 소유자에게 보증금 환급
    @Transactional
    fun failDueTo(reservationId: Long, reason: String, failDue: FailDue) {
        val reservation = findReservationByIdOrThrow(reservationId)

        // 보증금 상태 변경 및 반환 사유 업데이트 -> 사유를 받아와서 작성
        val depositHistory: DepositHistory = depositHistoryService.findDepositHistoryByReservationId(reservationId)
        depositHistoryService.updateDepositHistory(
            depositHistory.id!!,
            DepositStatus.RETURNED,
            ReturnReason.valueOf(reason)
        )

        if (failDue == FailDue.OWNER_ISSUE) {
            reservation.failDueToOwnerIssue()
            // 대여자 크레딧 업데이트
            userService.updateUserCredit(reservation.renter.id, depositHistory.amount)
        } else if (failDue == FailDue.RENTER_ISSUE) {
            reservation.failDueToRenterIssue()
            // 소유자 크레딧 업데이트
            userService.updateUserCredit(reservation.owner.id, depositHistory.amount)
        }

        // 알림 전송
        notificationService.createNotification(
            reservation.renter.id,
            "[${reservation.id}] '${reservation.post.getTitle()}' 대여가 실패했습니다."
        )
        notificationService.createNotification(
            reservation.owner.id,
            "[${reservation.id}] '${reservation.post.getTitle()}' 대여가 실패했습니다."
        )
    }

    // 예약 ID로 예약 조회
    fun getReservationById(reservationId: Long): ReservationResponse {
        val reservation = findReservationByIdOrThrow(reservationId)
        return ReservationResponse(
            reservation.id!!,
            reservation.status.name,
            reservation.post.id,
            reservation.startTime,
            reservation.endTime,
            reservation.amount,
            reservation.rejectionReason.toString(),
            reservation.owner.id!!,
            reservation.renter.id!!
        )
    }

    // 예외 처리 포함 예약 조회 메서드
    fun findReservationByIdOrThrow(reservationId: Long): Reservation {
        return reservationRepository.findById(reservationId)
            .orElseThrow {
                CustomException(
                    ErrorResponse(
                        "예약 조회 실패",
                        404,
                        "해당 ID의 예약을 찾을 수 없습니다.",
                        URI.create(
                            ServletUriComponentsBuilder.fromCurrentRequestUri().toUriString()
                        )
                    )
                )
            }!!
    }

    @Transactional(readOnly = true)
    fun getReservationsByPostId(postId: Long): List<ReservationResponse> {
        val reservations = reservationRepository.findByPostId(postId)
        val responses: MutableList<ReservationResponse> = ArrayList()
        val includedStatuses = listOf(
            ReservationStatus.APPROVED,
            ReservationStatus.IN_PROGRESS
        )
        reservations.stream()
            .filter { reservation: Reservation -> includedStatuses.contains(reservation.status) }
            .forEach { reservation: Reservation ->
                responses.add(
                    ReservationResponse(
                        reservation.id!!,
                        reservation.status.name,
                        reservation.post.id,
                        reservation.startTime,
                        reservation.endTime,
                        reservation.amount,
                        reservation.rejectionReason.toString(),
                        reservation.owner.id,
                        reservation.renter.id
                    )
                )
            }
        return responses
    }

    // 예약용 Post 조회 추가
    @Transactional(readOnly = true)
    fun getPostById(postId: Long): PostReservationResponse {
        val post = postRepository.findById(postId)
        return PostReservationResponse(
            post.get().getId(),
            post.get().getUser().id,
            post.get().getTitle(),
            post.get().getPriceType().name,
            post.get().getPrice()
        )
    }

    // 렌탈 예약 정보 DB에서 조회
    @Transactional(readOnly = true)
    fun getRentalReservations(userId: Long): List<Reservation> {
        return reservationRepository.findByOwnerId(userId)
    }

    // 대여 예약 정보 DB에서 조회
    @Transactional(readOnly = true)
    fun getBorrowReservations(userId: Long): List<Reservation> {
        return reservationRepository.findByRenterId(userId)
    }

    @Transactional(readOnly = true)
    fun getReservationByIdForReview(reservationId: Long): Optional<Reservation?> {
        return reservationRepository.findById(reservationId)
    }
}