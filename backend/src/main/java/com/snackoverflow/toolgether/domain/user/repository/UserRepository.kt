package com.snackoverflow.toolgether.domain.user.repository;

import com.snackoverflow.toolgether.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Optional<User> findById(long id);

    User findByNickname(String nickname);

    User findByPhoneNumber(String phoneNumber);

    @Query("SELECT u FROM User u LEFT JOIN Review r ON r.reviewee = u AND r.createdAt >= :date WHERE r.reviewee IS NULL")
    List<User> findUsersWithoutReviewsSince(LocalDateTime date);

    @Query("SELECT u.profileImage FROM User u WHERE u.profileImage IS NOT NULL")
    List<String> findAllProfileImageUrl();
}
