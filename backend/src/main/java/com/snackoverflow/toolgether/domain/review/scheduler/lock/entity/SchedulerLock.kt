package com.snackoverflow.toolgether.domain.review.scheduler.lock.entity

import jakarta.persistence.Entity
import jakarta.persistence.Id
import lombok.Getter
import lombok.Setter
import java.time.LocalDateTime

@Entity
data class SchedulerLock(
    //락 이름(스케줄러 작업 이름)
    @Id
    var lockName: String,
    //락 획득 시간
    var lockedAt: LocalDateTime
)
