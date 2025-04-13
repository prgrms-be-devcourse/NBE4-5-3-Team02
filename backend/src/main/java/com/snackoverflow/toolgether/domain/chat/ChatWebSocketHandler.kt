package com.snackoverflow.toolgether.domain.chat

import com.fasterxml.jackson.databind.ObjectMapper
import com.snackoverflow.toolgether.domain.chat.dto.ChatMessage
import com.snackoverflow.toolgether.domain.chat.dto.CommunityMessage
import com.snackoverflow.toolgether.domain.chat.redis.RedisPubSubEventPublisher
import com.snackoverflow.toolgether.domain.chat.redis.TopicFactory
import com.snackoverflow.toolgether.domain.chat.service.ChannelSessionService
import com.snackoverflow.toolgether.domain.chat.service.TopicSubscriptionService
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Component
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.PingMessage
import org.springframework.web.socket.PongMessage
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketHttpHeaders
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.io.IOException
import java.net.URI
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit

@Component
class ChatWebSocketHandler(
    private val redisPublisher: RedisPubSubEventPublisher, // Redis 메시지 발행자
    private val objectMapper: ObjectMapper, // JSON 처리
    private val topicFactory: TopicFactory, // 채널(토픽) 생성
    private val topicSubscriptionService: TopicSubscriptionService, // 채널 구독 용도
    private val channelSessionService: ChannelSessionService,
    private val redisTemplate: RedisTemplate<String, Any>,
    private val log: Logger,
    @Value("\${websocket.endpoint.uri}") private val socketUri: String
) : TextWebSocketHandler() {

    private val pongReceivedMap = ConcurrentHashMap<WebSocketSession, Boolean>()
    private val scheduler: ScheduledExecutorService = Executors.newScheduledThreadPool(1) // 재연결 스케줄러

    @Async
    override fun afterConnectionEstablished(session: WebSocketSession) {
        // 세션에서 userId 추출 (안전한 캐스팅)
        val userId = session.attributes["userId"] as? String ?: run {
            log.error("Invalid userId in session attributes")
            return
        }

        log.info("클라이언트 연결 성공: {}, userId: {}", session.id, userId)

        // 읽지 않은 메시지 처리
        val unreadKey = "chat:unread:$userId"
        redisTemplate.opsForValue()[unreadKey]?.let { unreadCount ->
            if (unreadCount is Int && unreadCount > 0) {
                session.sendMessage(TextMessage("읽지 않은 메시지가 $unreadCount 개 있습니다."))
                redisTemplate.delete(unreadKey)
            }
        }

        // Pong 상태 초기화 및 핑 스케줄러 시작
        pongReceivedMap[session] = true
        startPingScheduler(session)
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        // 메시지 처리 (Redis Pub/Sub으로 발행)
        log.info("message 변환 전: {}", message.payload)
        val payload = message.payload

        // JSON 데이터를 파싱하여 메시지 유형 확인
        val jsonNode = objectMapper.readTree(payload)
        log.info("jsonNode 확인: {}", jsonNode)

        if (jsonNode.has("region")) {
            // 단체 채팅 메시지 처리
            val communityMessage = objectMapper.readValue(payload, CommunityMessage::class.java)

            // 지역에 따라 적절한 채널 선택
            val region = communityMessage.region // 예: "강남구", "마포구"
            val channelTopic = ChannelTopic("chatroom:$region")

            // 채널 구독 설정 및 WebSocket 세션 등록
            topicSubscriptionService.subscriberToChatCommunity(channelTopic)
            channelSessionService.addSession(channelTopic.topic, session)

            log.debug("message 변환 후: {}, 채널 생성 및 Redis 발행 준비", communityMessage)

            // Redis로 메시지 발행
            redisPublisher.publishCommunityEvent(communityMessage)
        } else {
            // 개인 채팅 메시지 처리
            val chatMessage = objectMapper.readValue(payload, ChatMessage::class.java)

            // 토픽 생성 및 구독 설정
            val channelTopic = topicFactory.create(chatMessage.sender, chatMessage.receiver)
            topicSubscriptionService.subscribeToChatTopic(chatMessage.sender, chatMessage.receiver)

            // WebSocket 세션을 해당 채널에 등록
            channelSessionService.addSession(channelTopic.topic, session)

            log.debug("message 변환 후: {}, 채널 생성 및 Redis 발행 준비", chatMessage)

            // Redis로 메시지 발행
            redisPublisher.publishChatEvent(channelTopic.topic, chatMessage)
        }
    }

    // WebSocket 연결 종료 시 호출되는 메서드
    @Async
    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        log.info("WebSocket 연결 종료: {}, 상태: {}", session.id, status)

        // 세션 제거
        pongReceivedMap.remove(session)
        channelSessionService.removeSession(session)

        // 자동 재연결 로직 실행
        attemptReconnect(session)
    }

    // Pong 메시지 처리
    override fun handlePongMessage(session: WebSocketSession, message: PongMessage) {
        log.info("Pong 메시지 수신: {}", session.id)
        pongReceivedMap[session] = true // Pong 수신 상태 업데이트
    }

    private fun startPingScheduler(session: WebSocketSession) {
        scheduler.scheduleAtFixedRate({
            try {
                // 세션이 열려있는지 확인
                if (session.isOpen) {
                    // Pong 응답 확인
                    if (!pongReceivedMap.getOrDefault(session, false)) {
                        log.warn("Pong 메시지를 받지 못함. 세션 종료 및 재연결 시도: {}", session.id)
                        session.close()
                        attemptReconnect(session)
                        return@scheduleAtFixedRate
                    }

                    // Ping 메시지 전송 및 상태 업데이트
                    session.sendMessage(PingMessage())
                    pongReceivedMap[session] = false
                    log.info("Ping 메시지 전송: {}", session.id)
                }
            } catch (e: IOException) {
                log.error("Ping 메시지 전송 실패: ${e.message}", e)
                attemptReconnect(session)
            }
        }, 0, 30, TimeUnit.SECONDS) // 초기 지연 0, 30초 주기 실행
    }

    private fun attemptReconnect(oldSession: WebSocketSession) {
        scheduler.schedule({
            try {
                // 세션에서 userId 추출 (널 안전성 처리)
                val userId = oldSession.attributes["userId"] as? String

                userId?.let { uid ->
                    // 신규 WebSocket 연결 동기 생성
                    val newSession = createNewWebSocketConnection(uid)

                    // 연결 성공 시 채널 재구독
                    if (newSession?.isOpen == true) {
                        val channels = channelSessionService.getChannelsByUserId(uid) ?: emptySet()
                        channels.forEach { channel ->
                            channelSessionService.addSession(channel, newSession)
                            log.info("WebSocket 재연결 성공: 세션 ID = ${newSession.id}, 채널 = $channel")
                        }
                    }
                }
            } catch (e: Exception) {
                log.error("WebSocket 재연결 중 예외 발생: ${e.message}", e)
            }
        }, 30, TimeUnit.SECONDS) // 30초 후 재연결 시도
    }


    private fun createNewWebSocketConnection(userId: String): WebSocketSession {
        val client = StandardWebSocketClient()
        val uri = URI("$socketUri?userId=$userId") // 문자열 템플릿으로 URI 생성

        try {
            log.info("WebSocket 재연결 성공")
            return client.execute(
                this,
                WebSocketHttpHeaders(),  // 빈 헤더 생성
                uri
            ).get()
        } catch (e: Exception) {
            log.error("WebSocket 연결 생성 중 예외 발생: ${e.message}", e)
            throw RuntimeException("WebSocket 연결 생성 실패", e)
        }
    }
}