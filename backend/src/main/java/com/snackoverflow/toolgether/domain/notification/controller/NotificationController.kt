package com.snackoverflow.toolgether.domain.notification.controller

import com.snackoverflow.toolgether.domain.notification.dto.NotificationDto
import com.snackoverflow.toolgether.domain.notification.service.NotificationService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService
) {
    // 사용자 ID로 읽지 않은 알림 목록 조회
    @GetMapping("/unread/{userId}")
    fun getUnreadNotifications(@PathVariable userId: Long): ResponseEntity<List<NotificationDto>> {
        val notifications = notificationService.getUnreadNotifications(userId)
        return ResponseEntity.ok(notifications)
    }

    // 알림 읽음 처리
    @PutMapping("/{notificationId}")
    fun markNotificationAsRead(@PathVariable notificationId: Long): ResponseEntity<*> {
        try {
            notificationService.markNotificationAsRead(notificationId)
            return ResponseEntity.ok().build<Any>()
        } catch (e: RuntimeException) {
            return ResponseEntity.notFound().build<Any>()
        }
    }
}