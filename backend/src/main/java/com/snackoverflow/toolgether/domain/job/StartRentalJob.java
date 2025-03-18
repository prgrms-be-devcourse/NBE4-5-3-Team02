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
public class StartRentalJob implements Job {

	@Autowired
	private ReservationService reservationService;

	@Override
	public void execute(JobExecutionContext context) {
		JobDataMap jobDataMap = context.getMergedJobDataMap(); // 수정: getMergedJobDataMap() 사용
		Object reservationIdObj = jobDataMap.get("reservationId");
		Long reservationId = null;
		if(reservationIdObj instanceof Long){
			reservationId = (Long) reservationIdObj;
		}


		if (reservationId == null) {
			log.error("reservationId is null in StartRentalJob!"); // null인 경우 로그 출력
			return; // null이면 작업 중단
		}

		log.info("StartRentalJob executed for reservationId: {}", reservationId);
		log.info("StartRentalJob: reservationService is null? {}", reservationService == null);

		log.info("StartRentalJob executed for reservationId: {}", reservationId);
		// startTime이 현재 시간보다 이전인지 확인하는 로직 추가
		LocalDateTime now = LocalDateTime.now();
		Reservation reservation = reservationService.findReservationByIdOrThrow(reservationId);

		log.info("StartRentalJob - now: {}, startTime: {}, reservation: {}", now, reservation.getStartTime(), reservation);
		if (reservation.getStartTime().isBefore(now)) {
			reservationService.startRental(reservationId);
		}
	}
}