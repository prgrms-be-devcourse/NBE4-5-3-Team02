package com.snackoverflow.toolgether.domain.chat.service;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChannelSessionService {

    // 채널별로 세션을 관리 (동일 채팅방 생성 방지를 위해 set 적용)
    private final Map<String, Set<WebSocketSession>> channelSessionsMap = new ConcurrentHashMap<>();

    // ConcurrentHashMap과 ConcurrentSet 를 이용해 동시성 문제 해결 및 성능 개선
    public void addSession(String channelName, WebSocketSession session) {
        channelSessionsMap.computeIfAbsent(channelName, k -> Collections.newSetFromMap(new ConcurrentHashMap<>())).add(session);
    }

    public void removeSession(WebSocketSession session) {
        for (Set<WebSocketSession> sessions : channelSessionsMap.values()) {
            sessions.remove(session);
        }
    }

    public Set<WebSocketSession> getSessions(String channelName) {
        return channelSessionsMap.getOrDefault(channelName, Collections.emptySet());
    }
}