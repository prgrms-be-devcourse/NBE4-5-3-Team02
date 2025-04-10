package com.snackoverflow.toolgether.global.config

import com.snackoverflow.toolgether.domain.job.CompleteRentalJob
import com.snackoverflow.toolgether.domain.job.StartRentalJob
import org.quartz.*
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class QuartzConfig {
	@Bean
	fun startRentalJobDetail(): JobDetail {  //JobDetail 생성
		return JobBuilder.newJob(StartRentalJob::class.java) // 어떤 Job을 실행시킬 것인가?
			.withIdentity("startRentalJob") // Job 이름
			.storeDurably() //DB에 저장되어 스케줄러가 다시 시작되어도 유지가 됨.
			.build()
	}

	@Bean
	fun completeRentalJobDetail(): JobDetail { // JobDetail
		return JobBuilder.newJob(CompleteRentalJob::class.java) // 어떤 Job을 실행시킬 것인가?
			.withIdentity("completeRentalJob") // Job 이름
			.storeDurably() //DB에 저장
			.build()
	}


	// startTime에 Trigger
	@Bean
	fun startRentalTrigger(@Qualifier("startRentalJobDetail") startRentalJobDetail: JobDetail?): Trigger { // Trigger 생성
		//SimpleScheduleBuilder는 특정 시간에 한번만 실행하는 Trigger
		return TriggerBuilder.newTrigger()
			.forJob(startRentalJobDetail) // 어떤 JobDetail에 Trigger를 걸 것인가?
			.withIdentity("startRentalTrigger") // 트리거 이름
			// .startAt()  // 언제 실행할 것인가? -> 서비스에서 처리
			.withSchedule(
				SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow()
			) // 놓친 스케줄은 즉시 실행
			.build()
	}

	// endTime에 Trigger
	@Bean
	fun completeRentalTrigger(@Qualifier("completeRentalJobDetail") completeRentalJobDetail: JobDetail?): Trigger { // Trigger
		return TriggerBuilder.newTrigger()
			.forJob(completeRentalJobDetail) // 어떤 JobDetail에 Trigger를 걸 것인가?
			.withIdentity("completeRentalTrigger") // 트리거 이름
			//.startAt() // 서비스에서 처리
			.withSchedule(SimpleScheduleBuilder.simpleSchedule().withMisfireHandlingInstructionFireNow())
			.build()
	}
}