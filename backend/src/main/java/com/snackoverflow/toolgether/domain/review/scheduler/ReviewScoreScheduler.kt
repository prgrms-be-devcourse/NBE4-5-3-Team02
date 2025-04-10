package com.snackoverflow.toolgether.domain.review.scheduler

import com.snackoverflow.toolgether.domain.review.entity.Review
import com.snackoverflow.toolgether.domain.review.scheduler.lock.entity.SchedulerLock
import com.snackoverflow.toolgether.domain.review.scheduler.lock.repository.SchedulerLockRepository
import com.snackoverflow.toolgether.domain.review.service.ReviewService
import com.snackoverflow.toolgether.domain.user.entity.User
import com.snackoverflow.toolgether.domain.user.service.UserService
import com.snackoverflow.toolgether.global.util.ScheduleUtil
import org.springframework.retry.annotation.Backoff
import org.springframework.retry.annotation.Recover
import org.springframework.retry.annotation.Retryable
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import kotlin.math.max
import kotlin.math.min

@Component
@Retryable(value = [Exception::class], maxAttempts = 3, backoff = Backoff(delay = 10000))
class ReviewScoreScheduler(
    private val reviewService: ReviewService,
    private val userService: UserService,
    private val schedulerLockRepository: SchedulerLockRepository,
) {
    companion object {
        private const val LOCK_NAME = "aggregateLock"
        // 감쇠 관련 상수 - 6개월 이내 리뷰가 없을 경우 매주 감점할 점수
        private const val DECAY_RATE = 5.0
        // 기본 점수
        private const val BASE_SCORE = 30.0
    }

    @Scheduled(cron = "0 0 0 ? * MON")
    @Transactional
    fun aggregateReviewScores() {
        println("------------점수 집계 작업 작동------------")

        // 락 획득 시도
        val lockOptional = schedulerLockRepository.findByLockNameWithLock(LOCK_NAME)
        if (lockOptional.isPresent) {
            println("------------락 획득 실패 - 이미 실행 중------------")
            return
        }
        println("------------락 획득 성공------------")

        val schedulerLock = SchedulerLock(LOCK_NAME, LocalDateTime.now())
        schedulerLockRepository.save(schedulerLock)
        println("------------점수 집계 시작------------")

        try {
            // 새 리뷰 집계: 지난 1주일 동안 생성된 리뷰만 대상
            val oneWeekAgo = LocalDateTime.now().minusWeeks(1)
            val newReviews = reviewService.getReviewsCreatedAfter(oneWeekAgo)
            println("새롭게 집계할 리뷰 개수: $newReviews.size")

            // 새 리뷰가 있다면, 사용자별로 그룹화하고, 각 리뷰별로 점수 변동폭을 산출하여 합산
            if (!newReviews.isEmpty()) {
                val reviewsByUser = newReviews.groupBy {
                    it.reviewee
                }
                println("새 리뷰를 가진 사용자 수: $reviewsByUser.size")

                reviewsByUser.forEach { (user: User, reviews: List<Review>) ->
                    println("처리할 사용자 ID: ${user.id}") // 추가된 로그
                    var totalChange = 0.0
                    for ((_, _, _, _, productScore, timeScore, kindnessScore, _) in reviews) {
                        println("리뷰 점수 - product: $productScore, time: $timeScore, kindness: $kindnessScore") // 추가된 로그
                        // 단건 리뷰의 평균 점수 계산
                        val reviewAvg = (productScore + timeScore + kindnessScore) / 3.0
                        println("리뷰 평균 점수: $reviewAvg") // 추가된 로그
                        // 리뷰에 대해 결정된 점수 변화량 (예: 3점 -> +0.5, 5점 -> +1.0, 2점 -> -0.5)
                        val change = ScheduleUtil.determineScoreChange(reviewAvg)
                        println("점수 변화량: $change") // 추가된 로그
                        totalChange += change
                    }
                    val updatedScore = max(0.0, min(100.0, user.score + totalChange))
                    println(
                        "유저id: " + user.id +
                                ", 기존 점수: " + user.score +
                                ", 새 리뷰 총 변화량: " + totalChange +
                                ", 업데이트 후 점수: " + updatedScore
                    )
                    userService.updateUserScore(user.id, updatedScore)
                }
            } else {
                println("해당 유저의 지난 1주일 동안 새 리뷰가 없습니다.")
            }

            // 감쇠 로직: 최근 6개월 동안 새 리뷰가 전혀 없는 사용자에 대해, 매주 5점씩 감점 (단, 점수는 30점 이하로 내려가지 않음)
            val sixMonthsAgo = LocalDateTime.now().minusMonths(6)
            val inactiveUsers = userService.getUsersWithoutReviewsSince(sixMonthsAgo)
            println("최근 6개월 새 리뷰가 없는 사용자 수: $inactiveUsers.size")
            for (user in inactiveUsers) {
                if (user.score > BASE_SCORE) {
                    val decayedScore = max(BASE_SCORE, user.score - DECAY_RATE)
                    userService.updateUserScore(user.id, decayedScore)
                }
            }

            println("------------점수 집계 작업 완료------------")
        } finally {
            // 락 해제
            schedulerLockRepository.deleteById(LOCK_NAME)
            println("------------락 해제------------")
        }
    }

    @Recover
    fun recover(e: Exception) {
        System.err.println("------------점수 집계 작업 최종 실패!------------")
        System.err.println("에러 내용: " + e.message)
        System.err.println("실패 시간: " + LocalDateTime.now())
        System.err.println("------------원인을 파악해 수정하거나 집계를 수동으로 재시도하세요.------------")
    }


}
