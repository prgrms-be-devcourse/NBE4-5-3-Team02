package com.snackoverflow.toolgether.domain.reservation.controller

import com.snackoverflow.toolgether.domain.reservation.dto.PostReservationResponse
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationRequest
import com.snackoverflow.toolgether.domain.reservation.dto.ReservationResponse
import com.snackoverflow.toolgether.domain.reservation.entity.FailDue
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService
import com.snackoverflow.toolgether.global.dto.RsData
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/reservations")
class ReservationController(
    private val reservationService: ReservationService
) {
    @PostMapping("/request")
    fun createReservation(@RequestBody reservationRequest: ReservationRequest): RsData<ReservationResponse> {
        val response = reservationService.requestReservation(reservationRequest)
        return RsData("201-1", "예약 요청 성공", response)
    }

    // 예약 상태를 승인으로 바꾼 후 DepositHistory 생성
    @PatchMapping("/{id}/approve")
    fun approveReservation(@PathVariable id: Long): RsData<Void> {
        reservationService.approveReservation(id)
        return RsData(
            "201-1",
            "${id}번 예약 승인 성공"
        )
    }

    @PatchMapping("/{id}/reject")
    fun rejectReservation(@PathVariable id: Long, @RequestParam reason: String?): RsData<Void> {
        reservationService.rejectReservation(id, reason)
        return RsData(
            "200-1",
            "${id}번 예약 거절 성공"
        )
    }

    @PatchMapping("/{id}/cancel")
    fun cancelReservation(@PathVariable id: Long): RsData<Void> {
        reservationService.cancelReservation(id)
        return RsData(
            "200-1",
            "${id}번 예약 취소 성공"
        )
    }

    @PatchMapping("/{id}/start")
    fun startRental(@PathVariable id: Long): RsData<Void> {
        reservationService.startRental(id)
        return RsData(
            "200-1",
            "${id}번 예약 대여 시작 성공"
        )
    }

    @PatchMapping("/{id}/complete")
    fun completeRental(@PathVariable id: Long): RsData<Void> {
        reservationService.completeRental(id)
        return RsData(
            "200-1",
            "${id}번 예약 대여 종료 성공"
        )
    }

    @PatchMapping("/{id}/ownerIssue")
    fun ownerIssue(@PathVariable id: Long, @RequestParam reason: String): RsData<Void> {
        reservationService.failDueTo(id, reason, FailDue.OWNER_ISSUE)
        return RsData(
            "200-1",
            "${id}번 예약 소유자에 의한 이슈로 환급 성공"
        )
    }

    @PatchMapping("/{id}/renterIssue")
    fun renterIssue(@PathVariable id: Long, @RequestParam reason: String): RsData<Void> {
        reservationService.failDueTo(id, reason, FailDue.RENTER_ISSUE)
        return RsData(
            "200-1",
            "${id}번 예약 대여자에 의한 이슈로 환급 성공"
        )
    }

    @GetMapping("/{id}")
    fun getReservationById(@PathVariable id: Long): RsData<ReservationResponse> {
        val response = reservationService.getReservationById(id)
        return RsData(
            "200-1",
            "${id}번 예약 조회 성공",
            response
        )
    }

    @GetMapping("/reservatedDates/{id}")
    fun getReservedDates(@PathVariable id: Long): RsData<List<ReservationResponse>> {
        val reservations = reservationService.getReservationsByPostId(id)
        return RsData(
            "200-1",
            "${id}번 게시글의 예약 일정 조회 성공",
            reservations
        )
    }

    @GetMapping("/post/{postid}")
    fun getPostReservation(@PathVariable postid: Long): RsData<PostReservationResponse> {
        val p = reservationService.getPostById(postid)
        return RsData(
            "200-1",
            "${postid}번 게시글 조회 성공",
            p
        )
    }
}