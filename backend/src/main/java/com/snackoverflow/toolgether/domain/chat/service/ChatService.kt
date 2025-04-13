package com.snackoverflow.toolgether.domain.chat.service

import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import com.snackoverflow.toolgether.domain.chat.dto.ChatMessage
import com.snackoverflow.toolgether.domain.chat.redis.RedisPubSubEventPublisher.Companion.CHAT_EVENT_PREFIX
import com.snackoverflow.toolgether.global.constants.AppConstants.PERSONAL_CHAT_PREFIX
import com.snackoverflow.toolgether.global.exception.ErrorCode
import com.snackoverflow.toolgether.global.exception.ServiceException
import org.slf4j.Logger
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.data.redis.core.ZSetOperations
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit

@Service
@Transactional(readOnly = true)
class ChatService(
    private val redisTemplate: RedisTemplate<String, String>,
    private val objectMapper: ObjectMapper,
    private val log: Logger
) {

    @Transactional
    fun saveMessage(channelName: String, chatMessage: ChatMessage) {
        try {
            val messageData = createMessageData(chatMessage)
            val jsonMessage = objectMapper.writeValueAsString(messageData)
            val redisKey = PERSONAL_CHAT_PREFIX + channelName

            // Redis Sorted Set에 메시지 저장
            redisTemplate.opsForZSet().add(
                "$PERSONAL_CHAT_PREFIX$channelName",
                jsonMessage,
                timestampConvert(chatMessage.timeStamp)
            )

            log.info(
                "Redis Sorted Set 저장 = 채널:{}, 내용:{}, 시간:{}",
                channelName, jsonMessage, timestampConvert(chatMessage.timeStamp)
            )

            redisTemplate.opsForZSet().removeRange(redisKey, 0, -101)
        } catch (e: JsonProcessingException) {
            log.error("메시지 저장 중 JSON 처리 오류 발생: ${e.message}", e)
            throw ServiceException(ErrorCode.MESSAGE_SAVE_ERROR)
        }
    }

    private fun createMessageData(chatMessageDto: ChatMessage): Map<String, Any> {
        return mapOf(
            "sender" to chatMessageDto.sender,
            "receiver" to chatMessageDto.receiver,
            "content" to chatMessageDto.content,
            "senderName" to chatMessageDto.senderName,
            "receiverName" to chatMessageDto.receiverName,
            "deletedSender" to chatMessageDto.deletedSender,
            "deletedReceiver" to chatMessageDto.deletedReceiver
        )
    }

    // 특정 사용자의 전체 채팅 내역을 반환
    fun getChannels(userId: String): List<String> {
        val keys = redisTemplate.keys("$PERSONAL_CHAT_PREFIX:*")

        // 키가 없거나 비었을 경우 빈 리스트 반환
        keys?.ifEmpty { return emptyList() } ?: run {
            log.info("사용자 $userId 의 채널이 없습니다.")
            throw ServiceException(ErrorCode.NOT_FOUND_CHAT)
        }

        return keys.asSequence()
            .filter { it.contains(userId) } // userId 포함 키 필터링
            .filter { key ->
                redisTemplate.opsForZSet().size(key)?.let { it > 0 } ?: false // 메시지 존재 여부 확인
            }
            .map { it.replace(PERSONAL_CHAT_PREFIX, "") } // 접두사 제거
            .toList()
    }

    // 특정 채널에 대한 모든 내역을 반환
    fun getChatHistory(channelName: String, userId: String): List<ChatMessage> {
        log.info("Redis 에서 가져올 채팅: {}", channelName)

        // Redis에서 ZSet 데이터 조회 (값과 타임스탬프 함께 가져오기)
        val messagesWithScores = redisTemplate.opsForZSet()
            .rangeWithScores("$PERSONAL_CHAT_PREFIX$channelName", 0, -1)

        // 데이터가 없는 경우 빈 리스트 반환
        messagesWithScores ?: run {
            log.warn("채널 {}에 대한 채팅 내역이 없습니다.", channelName)
            return emptyList()
        }

        return messagesWithScores
            .asSequence()
            .map { tuple -> mapToChatMessage(tuple) } // Tuple -> ChatMessage 변환
            .filterNotNull() // null 값 필터링
            .filter { message -> shouldIncludeMessage(userId, message) } // 사용자 기준 필터링
            .toList()
    }

    // 특정 채널이 비어 있는지 확인
    private fun isChannelEmpty(redisKey: String): Boolean =
        redisTemplate.opsForZSet().size(redisKey)?.let { it == 0L } ?: true  // 메시지가 없으면 true 반환

    fun deleteChannelMessages(channelName: String, userId: String) {
        val redisKey = "$CHAT_EVENT_PREFIX$channelName"
        val chatHistory = getChatHistory(channelName, userId)

        if (chatHistory.isEmpty()) {
            log.info("채널 {}에 저장된 메시지가 없습니다.", channelName)
            return
        }

        chatHistory.forEach { message ->
            message.run {
                val score = timestampConvert(timeStamp)

                // 삭제 플래그 업데이트
                if (userId == sender) deletedSender = true
                if (userId == receiver) deletedReceiver = true

                // Redis에서 기존 메시지 삭제 후 재저장
                redisTemplate.opsForZSet().removeRangeByScore(redisKey, score, score)
                saveMessage(channelName, this)
            }
        }

        val allDeleted = chatHistory.all { it.deletedSender && it.deletedReceiver }
        if (allDeleted) {
            redisTemplate.expire(redisKey, 7, TimeUnit.DAYS)
            log.info("채널 {}에 대해 TTL 설정 완료 (7일 후 삭제)", channelName)
        }

        log.info("채널 {}의 메시지 논리 삭제 완료", channelName)
    }

    // 읽지 않은 메시지 카운트 가져오기
    fun getUnreadCount(userId: String): Int {
        val key = "chat:unread:$userId"
        return redisTemplate.opsForValue()[key]?.toIntOrNull() ?: 0
    }

    // 문자열 타임스탬프를 Unix 타임스탬프로 변환 (초 단위)
    private fun timestampConvert(timestamp: String): Double {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val localDateTime = LocalDateTime.parse(timestamp, formatter)
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli() / 1000.0
    }

    // Unix 타임스탬프를 문자열로 변환
    private fun convertUnixTimestampToString(unixTimestamp: Long): String {
        val epochMilli = (unixTimestamp * 1000).toLong()
        val localDateTime = Instant.ofEpochMilli(epochMilli).atZone(ZoneId.systemDefault()).toLocalDateTime()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return localDateTime.format(formatter)
    }

    // 메시지 포함 여부 결정
    private fun shouldIncludeMessage(userId: String, message: ChatMessage): Boolean = when {
        userId == message.sender -> !message.deletedSender
        userId == message.receiver -> !message.deletedReceiver
        else -> true
    }


    // Redis Tuple -> ChatMessage 매핑
    private fun mapToChatMessage(tuple: ZSetOperations.TypedTuple<String>): ChatMessage? {
        val json = tuple.value ?: run {
            log.error("튜플 값이 존재하지 않음")
            return null
        }
        val score = tuple.score?.toLong() ?: run {
            log.error("스코어 값이 존재하지 않음")
            return null
        }

        return try {
            objectMapper.readValue(json, ChatMessage::class.java).apply {
                timeStamp = convertUnixTimestampToString(score) // 타임스탬프 설정
            }
        } catch (e: JsonProcessingException) {
            log.error("REDIS -> SERVER 채팅 내역 파싱 오류 발생: ${e.message}")
            throw ServiceException(ErrorCode.MESSAGE_PARSE_ERROR, e)
        }
    }

    // 메시지 삭제 처리 로직
    private fun processMessageDeletion(channelName: String, userId: String, message: ChatMessage, redisKey: String) {
        // 타임스탬프를 Redis score로 변환
        val score = timestampConvert(message.timeStamp)

        // 발신자/수신자 여부에 따라 삭제 상태 업데이트
        when (userId) {
            message.sender -> message.deletedSender = true
            message.receiver -> message.deletedReceiver = true
        }

        // Redis에서 해당 점수의 기존 메시지 삭제
        redisTemplate.opsForZSet().removeRangeByScore(redisKey, score, score)

        // 수정된 메시지 재저장
        saveMessage(channelName, message)
    }

    // 전체 메시지 삭제 여부 확인
    private fun isAllMessagesDeleted(chatHistory: List<ChatMessage>): Boolean =
        chatHistory.all { it.deletedSender && it.deletedReceiver }
}

