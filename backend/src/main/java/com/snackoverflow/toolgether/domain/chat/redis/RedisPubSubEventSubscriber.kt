package com.snackoverflow.toolgether.domain.chat.redis

import com.fasterxml.jackson.databind.ObjectMapper
import com.snackoverflow.toolgether.domain.chat.dto.ChatMessage
import com.snackoverflow.toolgether.domain.chat.dto.CommunityMessage
import com.snackoverflow.toolgether.domain.chat.redis.RedisPubSubEventPublisher.ChatEvent
import com.snackoverflow.toolgether.domain.chat.redis.RedisPubSubEventPublisher.Companion.CHAT_EVENT_PREFIX
import com.snackoverflow.toolgether.domain.chat.redis.RedisPubSubEventPublisher.Companion.NOTIFICATION_PREFIX
import com.snackoverflow.toolgether.domain.chat.redis.RedisPubSubEventPublisher.NotificationPayload
import com.snackoverflow.toolgether.domain.chat.service.ChannelSessionService
import com.snackoverflow.toolgether.domain.chat.service.ChatNotificationService
import com.snackoverflow.toolgether.domain.chat.service.ChatService
import org.slf4j.Logger
import org.springframework.context.annotation.Lazy
import org.springframework.data.redis.connection.Message
import org.springframework.data.redis.connection.MessageListener
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.web.socket.TextMessage
import kotlin.jvm.java

@Service
@Lazy
class RedisPubSubEventSubscriber(
    private val log: Logger,
    private val objectMapper: ObjectMapper,
    private val chatNotificationService: ChatNotificationService,
    private val channelSessionService: ChannelSessionService,
    private val chatService: ChatService,
    private val redisTemplate: RedisTemplate<String, Any>,
) : MessageListener {

    companion object {
        const val UNREAD_COUNT_KEY_PREFIX = "chat:unread:"
    }

    // 채널 패턴별 메시지 처리
    override fun onMessage(message: Message, pattern: ByteArray?) {
        val channel = String(message.channel)
        val body = String(message.body)
        var isDelivered = false

        try {
            when {
                channel.startsWith(RedisPubSubEventPublisher.CHAT_EVENT_PREFIX) ->
                    handleChatEvent(channel, body, isDelivered)

                channel.startsWith(RedisPubSubEventPublisher.NOTIFICATION_PREFIX) ->
                    handleNotificationEvent(channel, body)

                channel.startsWith(RedisPubSubEventPublisher.COMMUNITY_EVENTS_CHANNEL) ->
                    handleCommunityEvent(channel, body)
            }
        } catch (e: Exception) {
            log.error("[Sub] 메시지 처리 실패 - 채널: $channel, 오류: ${e.message}")
        }
    }

    /**
     * 채팅 출력 예시
     * 채팅 타입: MSG, 채널: channel1
     * 보낸 사람: user1, 받는 사람: user2, 내용: Hello!
     */

    // 1. 채팅 메시지 처리
    private fun handleChatEvent(channel: String, messageBody: String, isDelivered: Boolean) {
        // 1. 이벤트 역직렬화
        val event = objectMapper.readValue(messageBody, ChatEvent::class.java)

        // 2. 페이로드 추출
        val chatMessageDto = objectMapper.readValue(event.payload, ChatMessage::class.java)
        log.info("보낸 사람: ${chatMessageDto.sender}, 받는 사람: ${chatMessageDto.receiver}, 내용: ${chatMessageDto.content}")

        // 4. 메시지 저장
        chatService.saveMessage(channel.removePrefix(CHAT_EVENT_PREFIX), chatMessageDto)

        // 5. 웹소켓 브로드캐스트 -> 현재 연결된 사용자들에게만 메시지 전달
        val sessions = channelSessionService.getSessions(channel)

        val isDelivered = sessions.any { session ->
            session.takeIf { it.isOpen }?.let {
                log.info("채널에 있는 세션: ${it.id}")
                it.sendMessage(TextMessage(objectMapper.writeValueAsString(chatMessageDto)))
                log.info("메시지 전송 완료: 세션ID=${it.id}, 메시지=${objectMapper.writeValueAsString(chatMessageDto)}")
                true
            } ?: false
        }

        // 세션이 없거나 모두 닫혀 있으면 읽지 않은 메시지 카운트 증가
        if (!isDelivered) {
            incrementUnreadCount(chatMessageDto.receiver)
        }

        log.info("[Sub] 채팅 메시지 전송 완료")
    }

    private fun incrementUnreadCount(receiver: String) {
        val key = "$UNREAD_COUNT_KEY_PREFIX$receiver"
        redisTemplate.opsForValue().increment(key)
        log.info("읽지 않은 메시지 수 증가: 사용자=$receiver, 키=$key")
    }

    private fun handleNotificationEvent(channel: String, messageBody: String) {
        // 1. 이벤트 역직렬화
        val event = objectMapper.readValue(messageBody, ChatEvent::class.java)

        // 2. 페이로드 추출
        val payload = objectMapper.readValue(event.payload, NotificationPayload::class.java)

        // 3. 알림 처리 로직
        chatNotificationService.sendNotification(
            userId = channel.removePrefix(NOTIFICATION_PREFIX),
            message = payload.msg,
            url = payload.url
        )

        log.info("[Sub] 알림 처리 완료 - 대상: ${event.channel}, 메시지: ${payload.msg}")
    }


    private fun handleCommunityEvent(channel: String, messageBody: String) {
        // 1. 이벤트 역직렬화 및 페이로드 추출
        val communityMessage = objectMapper.readValue(
            objectMapper.readValue(messageBody, ChatEvent::class.java).payload,
            CommunityMessage::class.java
        )

        // 2. 열려 있는 세션의 수 계산
        val sessions = channelSessionService.getSessions(channel)
        val openSessionCount = sessions.count { it.isOpen }

        // communityMessage에 세션 수 추가
        communityMessage.openSessionCount = openSessionCount


        // 5. 웹소켓 브로드캐스트
        sessions.filter { it.isOpen }.forEach { session ->
            session.sendMessage(TextMessage(objectMapper.writeValueAsString(communityMessage)))

            log.info("[Sub] 커뮤니티 메시지 전송 완료")
        }
    }
}