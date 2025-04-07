package com.snackoverflow.toolgether.domain.Notification.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.snackoverflow.toolgether.domain.Notification.entity.Notification;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findByUserIdAndIsRead(Long userId, Boolean isRead);
}
