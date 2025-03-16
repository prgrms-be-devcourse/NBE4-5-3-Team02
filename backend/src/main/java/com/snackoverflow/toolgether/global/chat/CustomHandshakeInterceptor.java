package com.snackoverflow.toolgether.global.chat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

@Slf4j
public class CustomHandshakeInterceptor implements HandshakeInterceptor {

    @Override
    public boolean beforeHandshake(
            ServerHttpRequest request,
            ServerHttpResponse response,
            WebSocketHandler wsHandler,
            Map<String, Object> attributes) throws Exception {

        // URI에서 쿼리 파라미터 추출
        log.info("인터셉터 실행");
        if (request instanceof ServletServerHttpRequest) {
            ServletServerHttpRequest servletRequest = (ServletServerHttpRequest) request;
            String query = servletRequest.getServletRequest().getQueryString();

            if (query != null && query.contains("userId")) {
                String userId = servletRequest.getServletRequest().getParameter("userId");
                attributes.put("userId", userId); // WebSocketSession에 저장
                log.info("웹소켓 세션에 저장한 user_id:{}", userId);
            }
        }
        return true; // 핸드셰이크 진행
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        // 핸드셰이크 후 처리
    }
}