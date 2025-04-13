package com.snackoverflow.toolgether.global.config;

import com.snackoverflow.toolgether.domain.chat.CustomHandshakeInterceptor;
import com.snackoverflow.toolgether.domain.chat.ChatWebSocketHandler
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker
import org.springframework.web.socket.config.annotation.StompEndpointRegistry
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer

@Configuration
@EnableWebSocket
@EnableWebSocketMessageBroker
class WebSocketConfig (
    private val chatWebSocketHandler: ChatWebSocketHandler,
    @Value("\${custom.site.frontUrl}") private val cors: String,
) : WebSocketConfigurer, WebSocketMessageBrokerConfigurer {

    // 웹소켓의 엔드포인트 정의 및 핸들러 등록
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketHandler, "/chat")
            .addInterceptors(CustomHandshakeInterceptor())
            .setAllowedOrigins(cors)
    }

    /**
     * topic: 브로드캐스트(broadcast) 방식으로 메시지를 전송
     * queue: 1:1 메시지 전송을 위한 큐(queue) 방식 (특정 사용자에게만 비공개 메시지)
     */

    override fun configureMessageBroker(config: MessageBrokerRegistry) {
        config.enableSimpleBroker("/topic", "/queue") // 구독 경로
        config.setApplicationDestinationPrefixes("/app") // 발행 경로
    }

    // 클라이언트가 /ws-stomp 엔드포인트로 WebSocket 연결을 수립
    override fun registerStompEndpoints(registry: StompEndpointRegistry) {
        registry.addEndpoint("/ws-stomp").setAllowedOrigins(cors).withSockJS()
    }
}