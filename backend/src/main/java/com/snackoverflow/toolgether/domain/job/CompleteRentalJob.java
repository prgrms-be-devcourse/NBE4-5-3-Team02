package com.snackoverflow.toolgether.domain.job;

import java.time.LocalDateTime;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import com.snackoverflow.toolgether.domain.reservation.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobDataMap;
import org.quartz.JobExecutionContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class CompleteRentalJob implements Job{

	@Autowired
	private ReservationService reservationService;

	@Override
	public void execute(JobExecutionContext context) {
		JobDataMap jobDataMap = context.getMergedJobDataMap();
		Object reservationIdObj = jobDataMap.get("reservationId");
		Long reservationId = null;

		if(reservationIdObj instanceof Long){
			reservationId = (Long) reservationIdObj;
		}

		if (reservationId == null) {
			log.error("reservationId is null in CompleteRentalJob!");
			return;
		}

		log.info("CompleteRentalJob executed for reservationId: {}", reservationId);

		// endTime이 현재 시간보다 이전인지 확인
		LocalDateTime now = LocalDateTime.now();
		Reservation reservation = reservationService.findReservationByIdOrThrow(reservationId);
		if(reservation.getEndTime().isBefore(now)){
			reservationService.completeRental(reservationId);
		}
	}
}