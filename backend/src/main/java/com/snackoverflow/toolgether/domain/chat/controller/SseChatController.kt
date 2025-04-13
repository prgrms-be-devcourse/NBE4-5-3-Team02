package com.snackoverflow.toolgether.domain.chat.controller

import com.snackoverflow.toolgether.domain.chat.sse.SseSubscriptionManager
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.MediaType
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter

@RestController
@RequestMapping("/api/sse")
class SseChatController(
    private val sseManager: SseSubscriptionManager
) {

    @GetMapping(path = ["/connect"], produces = [MediaType.TEXT_EVENT_STREAM_VALUE])
    fun connectSse(
        @RequestHeader("X-User-Id") userId: String,
        response: HttpServletResponse
    ): SseEmitter {

        return sseManager.subscribe(userId)
    }
}