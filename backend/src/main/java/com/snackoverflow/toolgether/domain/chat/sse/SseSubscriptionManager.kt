package com.snackoverflow.toolgether.domain.chat.sse

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.Logger
import org.springframework.stereotype.Component
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap

@Component
class SseSubscriptionManager(
    private val log: Logger,
    private val objectMapper: ObjectMapper
) {

    // 사용자 ID별 SSE Emitter 저장 (ConcurrentHashMap 사용 -> 동시성 환경)
    private val userEmitters = ConcurrentHashMap<String, SseEmitter>()

    // 타임아웃 및 재연결 설정
    companion object {
        private const val SSE_TIMEOUT = 30L * 60 * 1000 // 30분
        private const val RECONNECT_DELAY = 3000L
    }

    fun subscribe(userId: String): SseEmitter {
        val emitter = SseEmitter(SSE_TIMEOUT).apply {
            // 연결 종료 시 자동 정리
            onCompletion { userEmitters.remove(userId) }
            onError { ex ->
                log.error("SSE 연결 오류: ${ex.message}")
                userEmitters.remove(userId)
            }
        }

        // 초기 연결 확인 메시지
        sendEvent(emitter, "connect", "SSE 연결 성공")

        userEmitters[userId] = emitter
        return emitter
    }

    fun sendEvent(emitter: SseEmitter, eventName: String, data: Any) {
        try {
            emitter.send(
                SseEmitter.event()
                    .name(eventName)
                    .data(objectMapper.writeValueAsString(data))
                    .reconnectTime(RECONNECT_DELAY)
            )
        } catch (ex: IOException) {
            log.error("이벤트 전송 실패: ${ex.message}")
            emitter.completeWithError(ex)
        }
    }
}