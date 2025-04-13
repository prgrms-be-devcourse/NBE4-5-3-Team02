package com.snackoverflow.toolgether.domain.chat.service

import org.springframework.stereotype.Service
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.ConcurrentHashMap

@Service
class ChannelSessionService {

    private val channelSessionsMap: MutableMap<String, MutableSet<WebSocketSession>> = ConcurrentHashMap()

    fun addSession(channelName: String, session: WebSocketSession) {
        channelSessionsMap.computeIfAbsent(channelName) { ConcurrentHashMap.newKeySet() }.add(session)
    }

    // 특정 채널에서 세션 제거
    fun removeSession(channelName: String, session: WebSocketSession) {
        channelSessionsMap[channelName]?.let { sessions ->
            sessions.remove(session)
            if (sessions.isEmpty()) {
                channelSessionsMap.remove(channelName) // 채널이 비었으면 제거
            }
        }
    }

    // 전체 채널에서 세션 제거
    fun removeSession(session: WebSocketSession) {
        channelSessionsMap.keys.toSet().forEach { channelName ->
            removeSession(channelName, session)
        }
    }

    // 채널 이름으로 세션 조회
    fun getSessions(channelName: String): Set<WebSocketSession> {
        return channelSessionsMap[channelName] ?: emptySet()
    }

    // 사용자 ID 기반 채널 조회
    fun getChannelsByUserId(userId: String): Set<String> {
        return channelSessionsMap.entries
            .filter { (_, sessions) ->
                sessions.any { session ->
                    session.principal?.name == userId
                }
            }
            .map { it.key }
            .toSet()
    }
}
