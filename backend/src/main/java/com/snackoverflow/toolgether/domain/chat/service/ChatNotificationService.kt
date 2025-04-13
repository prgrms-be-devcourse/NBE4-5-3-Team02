package com.snackoverflow.toolgether.domain.chat.service

import com.snackoverflow.toolgether.domain.chat.sse.SseSubscriptionManager
import org.slf4j.Logger
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import java.time.Instant

@Service
class ChatNotificationService(
    private val redisTemplate: RedisTemplate<String, Any>,
    private val sseManager: SseSubscriptionManager,
    private val log: Logger
) {

    @Async
    fun sendNotification(userId: String, message: String, url: String) {

        val payload = mapOf(
            "message" to message,
            "url" to url,
            "timestamp" to Instant.now().epochSecond
        )

        sseManager.subscribe(userId)?.let { emitter ->
            sseManager.sendEvent(emitter, "notification", payload)
            log.info("알림 전송 - 대상 사용자 ID: $userId, 메시지: $message, URL: $url")
        } ?: run {
            log.warn("SSE 연결 없는 사용자 - $userId")
        }

        // 데이터베이스 저장
        redisTemplate.opsForList().rightPush("notifications:$userId", message)
    }
}