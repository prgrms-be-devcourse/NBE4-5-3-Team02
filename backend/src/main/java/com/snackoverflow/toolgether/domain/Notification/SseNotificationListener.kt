package com.snackoverflow.toolgether.domain.notification

import com.snackoverflow.toolgether.domain.reservation.controller.SseController
import lombok.extern.slf4j.Slf4j
import mu.KotlinLogging
import org.springframework.context.ApplicationListener
import org.springframework.stereotype.Component

@Slf4j
@Component
class SseNotificationListener(
    private val sseController: SseController
) : ApplicationListener<NotificationCreatedEvent> {
    private val log = KotlinLogging.logger {}
    override fun onApplicationEvent(event: NotificationCreatedEvent) {
        val userId = event.userId
        val message = event.message
        log.info("Received NotificationCreatedEvent. Sending SSE notification to user $userId, $message")
        sseController.sendNotification(userId, message)
    }
}