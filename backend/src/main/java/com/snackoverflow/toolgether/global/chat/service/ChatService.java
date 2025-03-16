package com.snackoverflow.toolgether.global.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackoverflow.toolgether.global.chat.dto.ChatMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public void saveMessage(String channelName, ChatMessage chatMessage) {
        Map<String, Object> messageData = new ConcurrentHashMap<>();
        messageData.put("sender", chatMessage.getSender());
        messageData.put("receiver", chatMessage.getReceiver());
        messageData.put("content", chatMessage.getContent());
        messageData.put("senderName", chatMessage.getSenderName());
        messageData.put("receiverName", chatMessage.getReceiverName());

        try {
            String jsonMessage = new ObjectMapper().writeValueAsString(messageData);
            redisTemplate.opsForZSet().add("chat:" + channelName, jsonMessage, timestampConvert(chatMessage.getTimeStamp()));
            log.info("Redis Sorted Set 저장 = 채널:{}, 내용:{}, 시간:{}", channelName,
                    jsonMessage, timestampConvert(chatMessage.getTimeStamp()));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    // 특정 사용자의 전체 채팅 내역을 반환
    public List<String> getChannels(String userId) {
        Set<String> keys = redisTemplate.keys("chat:*");
        if (keys == null || keys.isEmpty()) {
            return Collections.emptyList();
        }
        // userId가 포함된 키만 필터링하여 채널명 추출
        return keys.stream()
                .filter(key -> key.contains(userId)) // userId가 포함된 키만 선택
                .map(key -> key.replace("chat:", "")) // "chat:" 접두사 제거
                .toList();
    }

    // 특정 채널에 대한 모든 내역을 반환
    public List<ChatMessage> getChatHistory(String channelName) {
        log.info("Redis 에서 가져올 채팅:{}", channelName);

        // ZSet의 값과 score를 함께 가져옴
        Set<ZSetOperations.TypedTuple<String>> messagesWithScores =
                redisTemplate.opsForZSet().rangeWithScores("chat:" + channelName, 0, -1);

        if (messagesWithScores == null) {
            log.warn("채널 {}에 대한 채팅 내역이 없습니다.", channelName);
            return Collections.emptyList();
        }

        // JSON 데이터와 score를 ChatMessage 매핑
        List<ChatMessage> chatHistory = messagesWithScores.stream()
                .map(tuple ->
                {
                    String json = tuple.getValue();
                    Double score = tuple.getScore();
                    // Unix -> String
                    String timestamp = convertUnixTimestampToString(score);
                    log.info("불러온 채팅 내역:{}, {}", json, timestamp);

                    try {
                        ChatMessage response = objectMapper.readValue(json, ChatMessage.class);
                        if (timestamp != null) {
                            response.setTimeStamp(timestamp); // score를 timeStamp로 설정
                        }
                        return response;
                    } catch (JsonProcessingException e) {
                        log.error("REDIS -> SERVER 채팅 내역 파싱 오류 발생: {}", e.getMessage());
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .toList();
        for (ChatMessage response : chatHistory) {
            log.info("불러온 채팅 내역 -> 변환: {}", response);
        }
        return chatHistory;
    }

    // 채팅 삭제
    public void deleteChannelMessages(String channelName) {
        String redisKey = "chat:" + channelName;

        Boolean isDeleted = redisTemplate.delete(redisKey);

        if (isDeleted.equals(true)) {
            log.info("Redis Sorted Set 삭제 완료 - 채널: {}", channelName);
        } else {
            log.warn("Redis Sorted Set 삭제 실패 또는 존재하지 않음 - 채널: {}", channelName);
        }
    }

    // 읽지 않은 메시지 카운트
    public Integer getUnreadCount(String userId) {
        String key = "chat:unread:" + userId;
        String unread = redisTemplate.opsForValue().get(key);
        if (unread == null) {
            return 0;
        }
        return Integer.valueOf(unread);
    }

    private double timestampConvert(String timestamp) {
        // 문자열을 LocalDateTime으로 파싱
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        LocalDateTime localDateTime = LocalDateTime.parse(timestamp, formatter);

        // LocalDateTime을 밀리초 단위의 Unix 타임스탬프로 변환
        long epochMilli = localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();

        // double로 반환 (밀리초를 초 단위로 변환하려면 /1000.0)
        return epochMilli / 1000.0;
    }

    private String convertUnixTimestampToString(double unixTimestamp) {
        // Unix 타임스탬프를 밀리초 단위로 변환
        long epochMilli = (long) (unixTimestamp * 1000);

        // 밀리초를 LocalDateTime으로 변환
        LocalDateTime localDateTime = Instant.ofEpochMilli(epochMilli)
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        // LocalDateTime을 문자열로 변환
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return localDateTime.format(formatter);
    }
}

