package com.snackoverflow.toolgether.domain.job

import com.snackoverflow.toolgether.domain.reservation.service.ReservationService
import lombok.extern.slf4j.Slf4j
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class CompleteRentalJob : Job {
    @Autowired
    private val reservationService: ReservationService? = null

    override fun execute(context: JobExecutionContext) {
        val jobDataMap = context.mergedJobDataMap
        val reservationIdObj = jobDataMap["reservationId"]
        var reservationId: Long? = null

        if (reservationIdObj is Long) {
            reservationId = reservationIdObj
        }

        if (reservationId == null)
            return

        // endTime이 현재 시간보다 이전인지 확인
        val now = LocalDateTime.now()
        val reservation = reservationService!!.findReservationByIdOrThrow(reservationId)
        if (reservation.endTime.isBefore(now)) {
            reservationService.completeRental(reservationId)
        }
    }
}