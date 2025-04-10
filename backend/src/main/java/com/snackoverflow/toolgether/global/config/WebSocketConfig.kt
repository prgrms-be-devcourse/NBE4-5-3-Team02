package com.snackoverflow.toolgether.global.config;

import com.snackoverflow.toolgether.domain.chat.ChatWebSocketHandler;
import com.snackoverflow.toolgether.domain.chat.CustomHandshakeInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
class WebSocketConfig(
        private val chatWebSocketHandler: ChatWebSocketHandler
) : WebSocketConfigurer {

    @Value("\${custom.dev.frontUrl}")
    private lateinit var cors: String

    // 웹소켓의 엔드포인트 정의 및 핸들러 등록
    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketHandler, "/chat")
                .addInterceptors(CustomHandshakeInterceptor())
                .setAllowedOrigins(cors)
    }
}