package com.snackoverflow.toolgether.domain.chat.controller

import com.snackoverflow.toolgether.domain.chat.dto.ChatMessage
import com.snackoverflow.toolgether.domain.chat.service.ChatService
import com.snackoverflow.toolgether.global.dto.RsData
import org.slf4j.Logger
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/chat")
class ChatController(
    private val chatService: ChatService,
    private val log: Logger
) {

    // STOMP 메시지 발송 핸들러
    @MessageMapping("/chat")
    @SendTo("/topic/messages")
    fun sendMessage(message: ChatMessage) = message

    // 사용자 채널 목록 조회
    @GetMapping("/channels")
    fun getChannels(@RequestParam userId: String): RsData<Any> {

        val channels = chatService.getChannels(userId)

        return RsData(
            resultCode = "200-1",
            msg = "사용자가 포함된 채널 목록 조회 성공",
            data = channels
        )
    }

    // 채팅 내역 조회
    @GetMapping("/history")
    fun getChatHistory(
        @RequestParam channelName: String,
        @RequestParam userId: String
    ): RsData<Any> {

        val chatHistory = chatService.getChatHistory(channelName, userId)
        log.info("저장한 채팅 - 채널: {}, 내역: {}", channelName, chatHistory)
        return RsData(
            resultCode = "200-1",
            msg = "채팅 내역 불러오기 성공",
            data = chatHistory
        )
    }

    // 채팅방 삭제
    @DeleteMapping("/delete")
    fun deleteChat(
        @RequestParam channel: String,
        @RequestParam userId: String
    ): RsData<Boolean> {
        chatService.deleteChannelMessages(channel, userId)
        return RsData(
            resultCode = "201-1",
            msg = "채널 삭제 완료",
            data = true
        )
    }

    // 읽지 않은 메시지 카운트 조회
    @GetMapping("/unread-count")
    fun getUnreadCount(
        @RequestParam userId: String
    ): RsData<Int> {
        val unreadCount = chatService.getUnreadCount(userId)
        log.info("읽지 않은 메시지의 개수: {}", unreadCount)
        return RsData(
            resultCode = "200-1",
            msg = "읽지 않은 메시지의 수 조회 성공",
            data = unreadCount
        )
    }

}