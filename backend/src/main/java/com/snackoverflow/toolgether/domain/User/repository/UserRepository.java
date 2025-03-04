package com.snackoverflow.toolgether.domain.User.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.snackoverflow.toolgether.domain.User.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}
