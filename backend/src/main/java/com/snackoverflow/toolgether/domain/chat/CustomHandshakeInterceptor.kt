package com.snackoverflow.toolgether.domain.chat

import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.http.server.ServletServerHttpRequest
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.server.HandshakeInterceptor
import java.lang.Exception

class CustomHandshakeInterceptor() : HandshakeInterceptor {

    override fun beforeHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        attributes: MutableMap<String, Any>
    ): Boolean {

        // URI에서 쿼리 파라미터 추출
        if (request is ServletServerHttpRequest) {
            val servletRequest = request.servletRequest
            val query = servletRequest.queryString

            query?.takeIf { it.contains("userId") }?.let {
                val userId = servletRequest.getParameter("userId")
                attributes["userId"] = userId // WebSocketSession에 저장
            }
        }
        return true // 핸드셰이크 진행
    }

    override fun afterHandshake(
        request: ServerHttpRequest,
        response: ServerHttpResponse,
        wsHandler: WebSocketHandler,
        exception: Exception?
    ) {
        // 핸드셰이크 후 처리
    }
}