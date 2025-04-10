package com.snackoverflow.toolgether.domain.notification.service

import com.snackoverflow.toolgether.domain.notification.NotificationCreatedEvent
import com.snackoverflow.toolgether.domain.notification.dto.NotificationDto
import com.snackoverflow.toolgether.domain.notification.entity.Notification
import com.snackoverflow.toolgether.domain.notification.repository.NotificationRepository
import com.snackoverflow.toolgether.domain.user.service.UserService
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime
import java.util.*
import java.util.stream.Collectors

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val userService: UserService,
    private val publisher: ApplicationEventPublisher
) {
    // 알림 생성 및 저장
    @Transactional
    fun createNotification(userId: Long?, message: String): Notification {
        val notification: Notification = Notification(
            userService.findUserById(userId),
            message,
            LocalDateTime.now(),
            false
        )
        val savedNotification: Notification = notificationRepository.save(notification)

        // 이벤트 발행
        publisher.publishEvent(NotificationCreatedEvent(this,  userId ?: -1L, message))
        return savedNotification
    }

    // 사용자 ID로 읽지 않은 알림 목록 조회
    @Transactional(readOnly = true)
    fun getUnreadNotifications(userId: Long): List<NotificationDto> {
        return notificationRepository.findByUserIdAndIsRead(userId, false)
            .stream()
            .map { notification: Notification? ->
                NotificationDto(
                    notification!!
                )
            }
            .collect(Collectors.toList())
    }

    @Transactional
    fun markNotificationAsRead(notificationId: Long) {
        val notification: Optional<Notification?> = notificationRepository.findById(notificationId)
        if (notification.isPresent) {
            val n: Notification = notification.get()
            n.setIsRead(true)
            notificationRepository.save<Notification>(n)
        } else {
            throw RuntimeException("Notification not found")
        }
    }
}