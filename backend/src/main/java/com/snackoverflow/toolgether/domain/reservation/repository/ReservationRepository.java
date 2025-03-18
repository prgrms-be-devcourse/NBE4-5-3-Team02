package com.snackoverflow.toolgether.domain.reservation.repository;

import com.snackoverflow.toolgether.domain.reservation.entity.Reservation;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    // renterId를 기준으로 예약 정보를 조회하는 메서드
    List<Reservation> findByRenterId(Long renterId);

    // ownerId를 기준으로 예약 정보를 조회하는 메서드
    List<Reservation> findByOwnerId(Long ownerId);

    List<Reservation> findByPostId(Long postId);

    @Lock(LockModeType.PESSIMISTIC_WRITE) // 락 적용
    @Query("SELECT r FROM Reservation r WHERE r.post.id = :postId AND "
            + "(r.startTime < :endTime AND r.endTime > :startTime)")
    List<Reservation> findConflictingReservations(@Param("postId") Long postId,
                                                  @Param("startTime") LocalDateTime startTime,
                                                  @Param("endTime") LocalDateTime endTime);
}
