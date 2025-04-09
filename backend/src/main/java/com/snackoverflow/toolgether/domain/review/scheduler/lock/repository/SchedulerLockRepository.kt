package com.snackoverflow.toolgether.domain.review.scheduler.lock.repository

import com.snackoverflow.toolgether.domain.review.scheduler.lock.entity.SchedulerLock
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.util.*

interface SchedulerLockRepository : JpaRepository<SchedulerLock, String> {
    //락 획득 시 비관적 쓰기 락 적용(중복 호출 방지)
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select lock from SchedulerLock lock where lock.lockName = :lockName")
    fun findByLockNameWithLock(@Param("lockName") lockName: String): Optional<SchedulerLock>
}
