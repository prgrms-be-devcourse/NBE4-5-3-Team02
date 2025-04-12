package com.snackoverflow.toolgether.domain.chat.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.snackoverflow.toolgether.domain.chat.dto.ChatMessageDto
import com.snackoverflow.toolgether.domain.chat.dto.CommunityMessage
import com.snackoverflow.toolgether.domain.chat.service.ChatService
import org.slf4j.Logger
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

/**
 * Pub / Sub 의 발행 흐름
 * publishChatEvent (convertAndSend) -> 채널 브로드캐스트 -> onMessage -> handleChatEvent
 * 발행자     ↳ ChatEvent 생성              ↳ 메시지 전파        ↳ 역직렬화 및 저장 로직 실행
 */
@Component
class RedisPubSubEventPublisher(
    private val log: Logger,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val objectMapper: ObjectMapper,
    private val chatService: ChatService
) {

    companion object {
        const val CHAT_EVENT_PREFIX = "chat:event:"
        const val NOTIFICATION_PREFIX = "noti:event:"
        const val COMMUNITY_EVENTS_CHANNEL = "community:events:"
    }

    /**
     * Redis 에 발행된 메시지의 예시:
     * {
     *   "type": "MSG",
     *   "channel": "channel1",
     *   "payload": "{\"s\":\"user1\",\"r\":\"user2\",\"c\":\"Hello!\"}"
     * }
     */
    // 채팅 메시지 발행
    fun publishChatEvent(channel: String, message: ChatMessageDto) {

        val event = ChatEvent(
            type = "MSG",
            channel = channel,
            payload = objectMapper.writeValueAsString(message)
        )

        redisTemplate.convertAndSend("$CHAT_EVENT_PREFIX$channel", event)

        // Redis SortedSet에 값을 저장
        chatService.saveMessage(channel, message)

        log.info("[Pub] 채팅 발행 - 채널: $channel")
    }

    // 시스템 알림 발행
    fun publishNotification(userId: String, content: String) {
        val event = ChatEvent(
            type = "NOTI",
            channel = userId,
            payload = objectMapper.writeValueAsString(
                NotificationPayload(
                    msg = content,
                    url = "/notifications"
                )
            )
        )
        redisTemplate.convertAndSend("$NOTIFICATION_PREFIX$userId", event)
    }

    // 커뮤니티 메시지 발행
    fun publishCommunityEvent(
        message: CommunityMessage
    ) {

        val event = ChatEvent(
            type = "MSG",
            channel = message.region,
            payload = objectMapper.writeValueAsString(message)
        )

        redisTemplate.convertAndSend("${COMMUNITY_EVENTS_CHANNEL}:${message.region}", event)

        log.info("[Pub] 커뮤니티 이벤트 발행 - 지역: ${message.region}")
    }

    data class ChatEvent(
        val type: String,  // 이벤트 타입 (MESSAGE, NOTIFICATION 등)
        val channel: String,  // 대상 채널 (chat:room1)
        val payload: String,  // payload (실제 데이터)
        val ts: Long = System.currentTimeMillis()  // 이벤트 발생 시각 (Unix timestamp)
    )

    data class NotificationPayload(
        val msg: String,
        val url: String
    )
}