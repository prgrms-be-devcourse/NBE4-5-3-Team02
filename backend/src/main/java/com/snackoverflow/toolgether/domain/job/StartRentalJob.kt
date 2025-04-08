package com.snackoverflow.toolgether.domain.job

import com.snackoverflow.toolgether.domain.reservation.service.ReservationService
import org.quartz.Job
import org.quartz.JobExecutionContext
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class StartRentalJob : Job {
    @Autowired
    private val reservationService: ReservationService? = null

    override fun execute(context: JobExecutionContext) {
        val jobDataMap = context.mergedJobDataMap // 수정: getMergedJobDataMap() 사용
        val reservationIdObj = jobDataMap["reservationId"]
        var reservationId: Long? = null
        if (reservationIdObj is Long) {
            reservationId = reservationIdObj
        }


        if (reservationId == null)
            return  // null이면 작업 중단

        val now = LocalDateTime.now()
        val reservation = reservationService!!.findReservationByIdOrThrow(reservationId)

        if (reservation.startTime.isBefore(now)) {
            reservationService.startRental(reservationId)
        }
    }
}