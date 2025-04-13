package com.snackoverflow.toolgether.domain.chat.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackoverflow.toolgether.domain.chat.dto.ChatMessageDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
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
import java.util.concurrent.TimeUnit;

import static com.snackoverflow.toolgether.global.constants.AppConstants.PERSONAL_CHAT_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    // Redis 에는 채널당 100개의 메시지만 저장, 나머지는 DB에 저장한 후 영속적으로 관리 (삭제 전까지)
    public void saveMessage(String channelName, ChatMessageDto chatMessageDto) {
        // DB에 저장
        String senderId = chatMessageDto.getSender();
        String senderName = chatMessageDto.getSenderName();
        String receiverId = chatMessageDto.getReceiver();
        String receiverName = chatMessageDto.getReceiverName();
        String content = chatMessageDto.getContent();


        try {
            Map<String, Object> messageData = saveMessage(chatMessageDto);
            String jsonMessage = new ObjectMapper().writeValueAsString(messageData);
            redisTemplate.opsForZSet().add(
                    PERSONAL_CHAT_PREFIX +
                    channelName,
                    jsonMessage,
                    timestampConvert(chatMessageDto.getTimeStamp()));
            log.info("Redis Sorted Set 저장 = 채널:{}, 내용:{}, 시간:{}", channelName,
                    jsonMessage, timestampConvert(chatMessageDto.getTimeStamp()));

            // 최신 100개까지만 유지하도록 변경
            redisTemplate.opsForZSet().removeRange(
                    PERSONAL_CHAT_PREFIX + channelName,
                    0, -101); // 100개 초과시 오래된 항목 제거
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    @NotNull
    private Map<String, Object> saveMessage(ChatMessageDto chatMessageDto) {
        Map<String, Object> messageData = new ConcurrentHashMap<>();
        messageData.put("sender", chatMessageDto.getSender());
        messageData.put("receiver", chatMessageDto.getReceiver());
        messageData.put("content", chatMessageDto.getContent());
        messageData.put("senderName", chatMessageDto.getSenderName());
        messageData.put("receiverName", chatMessageDto.getReceiverName());
        messageData.put("deletedSender", chatMessageDto.isDeletedSender());
        messageData.put("deletedReceiver", chatMessageDto.isDeletedReceiver());
        return messageData;
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
                .filter(key -> !isChannelEmpty(key)) // 비어 있지 않은 채널만 선택
                .map(key -> key.replace("chat:", "")) // "chat:" 접두사 제거
                .toList();
    }

    // 특정 채널이 비어 있는지 확인
    private boolean isChannelEmpty(String redisKey) {
        Long size = redisTemplate.opsForZSet().size(redisKey);
        return size == null || size == 0; // 메시지가 없으면 true 반환
    }

    // 특정 채널에 대한 모든 내역을 반환
    public List<ChatMessageDto> getChatHistory(String channelName, String userId) {
        log.info("Redis 에서 가져올 채팅:{}", channelName);

        // ZSet의 값과 score를 함께 가져옴
        Set<ZSetOperations.TypedTuple<String>> messagesWithScores =
                redisTemplate.opsForZSet().rangeWithScores("chat:" + channelName, 0, -1);

        if (messagesWithScores == null) {
            log.warn("채널 {}에 대한 채팅 내역이 없습니다.", channelName);
            return Collections.emptyList();
        }

        // JSON 데이터와 score를 ChatMessage 매핑
        List<ChatMessageDto> chatHistory = messagesWithScores.stream()
                .map(tuple ->
                {
                    String json = tuple.getValue();
                    Double score = tuple.getScore();
                    // Unix -> String
                    String timestamp = convertUnixTimestampToString(score);

                    try {
                        ChatMessageDto response = objectMapper.readValue(json, ChatMessageDto.class);
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
                // 현재 사용자 기준으로 필터링
                .filter(message -> {
                    if (userId.equals(message.getSender())) {
                        return !message.isDeletedSender(); // sender인 경우 deletedSender가 false인 메시지만 포함
                    } else if (userId.equals(message.getReceiver())) {
                        return !message.isDeletedReceiver(); // receiver인 경우 deletedReceiver가 false인 메시지만 포함
                    }
                    return true; // 그 외의 경우 필터링하지 않음
                })
                .toList();
        for (ChatMessageDto response : chatHistory) {
            log.info("불러온 채팅 내역 -> 변환: {}", response);
        }
        return chatHistory;
    }

    @Transactional
    public void deleteChannelMessages(String channelName, String userId) {
        String redisKey = "chat:" + channelName;

        // 특정 채널의 채팅 내역 가져오기
        List<ChatMessageDto> chatHistory = getChatHistory(channelName, userId);

        if (chatHistory.isEmpty()) {
            log.info("채널 {}에 저장된 메시지가 없습니다.", channelName);
            return;
        }

        for (ChatMessageDto message : chatHistory) {
                // Redis에서 기존 점수(timeStamp)를 가져오기
                double score = timestampConvert(message.getTimeStamp());

                // sender와 receiver 비교하여 deletedSender와 deletedReceiver 업데이트
                if (userId.equals(message.getSender())) {
                    message.setDeletedSender(true);
                }
                if (userId.equals(message.getReceiver())) {
                    message.setDeletedReceiver(true);
                }

                // 기존 메시지 삭제 (점수를 기준으로)
                redisTemplate.opsForZSet().removeRangeByScore(redisKey, score, score);

                // 수정된 메시지를 저장 (기존 점수 유지)
                saveMessage(channelName, message);
        }

        // 모든 메시지가 논리적으로 삭제되었는지 확인
        boolean allDeleted = chatHistory.stream()
                .allMatch(msg -> msg.isDeletedSender() && msg.isDeletedReceiver());

        if (allDeleted) {
            redisTemplate.expire(redisKey, 7, TimeUnit.DAYS); // TTL 설정
            log.info("채널 {}에 대해 TTL 설정 완료 (7일 후 삭제)", channelName);
        }

        log.info("채널 {}의 메시지 논리 삭제 완료", channelName);
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

