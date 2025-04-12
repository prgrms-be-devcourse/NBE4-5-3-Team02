package com.snackoverflow.toolgether.domain.chat.redis

import com.snackoverflow.toolgether.domain.chat.dto.ChatMessageDto
import com.snackoverflow.toolgether.domain.chat.dto.CommunityMessage
import com.snackoverflow.toolgether.domain.chat.service.ChatService
import org.slf4j.Logger
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component

@Component
class RedisPublisher(
    private val log: Logger,
    private val redisTemplate: RedisTemplate<String, Any>, // RedisTemplate 사용
    private val chatService: ChatService
) {

    // 특정 채널에 메시지 발행
    fun publish(channelTopic: String, chatMessageDto: ChatMessageDto) {

        // Redis에 메시지 발행
        redisTemplate.convertAndSend(channelTopic, chatMessageDto)
        log.info("채널에 메시지를 발행, 채널 이름: $channelTopic")

        // Redis SortedSet에 값을 저장
        chatService.saveMessage(channelTopic, chatMessageDto)
    }

    // 지역 채팅방 리스트 -> 저장 안 함
    fun publishToCommunity(channelTopic: String, communityMessage: CommunityMessage) {

        redisTemplate.convertAndSend(channelTopic, communityMessage)
        log.info("채널에 메시지를 발행, 채널 이름: $channelTopic")
    }
}