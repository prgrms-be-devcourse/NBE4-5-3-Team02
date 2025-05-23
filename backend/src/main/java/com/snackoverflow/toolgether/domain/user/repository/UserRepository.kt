package com.snackoverflow.toolgether.domain.user.repository;

import com.snackoverflow.toolgether.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


interface UserRepository : JpaRepository<User, Long> {

    fun findByEmail(email: String): User?

    fun existsByEmail(email: String): Boolean

    @Query("select u from User u where u.id = :id")
    fun findByUserId(id: Long): User?

    fun existsByNickname(nickname: String): Boolean

    fun findByNickname(nickname: String): User?

    @Query("SELECT u FROM User u LEFT JOIN Review r ON r.reviewee = u AND r.createdAt >= :date WHERE r.reviewee IS NULL")
    fun findUsersWithoutReviewsSince(date: LocalDateTime): List<User>

    @Query("SELECT u.profileImage FROM User u WHERE u.profileImage IS NOT NULL")
    fun findAllProfileImageUrl(): List<String>

    fun findByphoneNumber(phoneNumber: String): User?
}