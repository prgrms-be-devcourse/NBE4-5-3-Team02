package com.snackoverflow.toolgether.domain.reservation.repository

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.LocalDateTime

interface ReservationRepository : JpaRepository<Reservation?, Long?> {
    fun findByRenterId(renterId: Long): List<Reservation>
    fun findByOwnerId(ownerId: Long): List<Reservation>
    fun findByPostId(postId: Long): List<Reservation>

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 락 적용
    @Query(
        ("SELECT r FROM Reservation r WHERE r.post.id = :postId AND "
                + "(r.startTime < :endTime AND r.endTime > :startTime)")
    )
    fun findConflictingReservations(
        @Param("postId") postId: Long?,
        @Param("startTime") startTime: LocalDateTime?,
        @Param("endTime") endTime: LocalDateTime?
    ): List<Reservation?>?
}