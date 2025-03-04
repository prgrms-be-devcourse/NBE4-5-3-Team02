package com.snackoverflow.toolgether.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.snackoverflow.toolgether.domain.user.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
