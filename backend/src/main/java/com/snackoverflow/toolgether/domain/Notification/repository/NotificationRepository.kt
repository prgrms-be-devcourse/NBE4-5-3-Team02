package com.snackoverflow.toolgether.domain.notification.repository

import com.snackoverflow.toolgether.domain.notification.entity.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification?, Long?> {
    fun findByUserIdAndIsRead(userId: Long, isRead: Boolean): List<Notification>
}