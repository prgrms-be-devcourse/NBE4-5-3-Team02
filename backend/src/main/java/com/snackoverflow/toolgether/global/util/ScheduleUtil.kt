package com.snackoverflow.toolgether.global.util

import java.time.LocalDateTime
import java.time.Period

object ScheduleUtil {
    // 1년간 리뷰의 생성 시간에 따른 가중치 계산 로직
    // 1달 이내: 1.0 / 6달 이내: 0.7 / 1년 이내: 0.3
    @JvmStatic
    fun calculateWeight(createAt: LocalDateTime): Double {
        val now = LocalDateTime.now()
        val period = Period.between(createAt.toLocalDate(), now.toLocalDate())
        val months = period.months + period.years * 12

        return if (months <= 1) 1.0 else if (months <= 6) 0.7 else 0.3
    }

    // 리뷰 점수 기반 점수 변동폭 결정 메서드
    // 2점 미만: -1.0 / 3점 미만: -0.5 / 4점 미만: 0.5 / 4점 이상: 1.0
    @JvmStatic
    fun determineScoreChange(averageScore: Double): Double {
        if (averageScore >= 1 && averageScore < 2) {
            return -1.0
        } else if (averageScore >= 2 && averageScore < 3) {
            return -0.5
        } else if (averageScore >= 3 && averageScore < 4) {
            return 0.5
        } else if (averageScore in 4.0..5.0) {
            return 1.0
        }
        //평균 1점 미만 5점 초과의 비정상적 리뷰
        return 0.0
    }
}