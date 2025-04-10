package com.snackoverflow.toolgether.domain.chat.redis;

import com.snackoverflow.toolgether.domain.chat.dto.ChatMessageDto;
import com.snackoverflow.toolgether.domain.chat.dto.CommunityMessage;
import com.snackoverflow.toolgether.domain.chat.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
// 특정 채널에 메시지를 발행
public class RedisPublisher {

    private final RedisTemplate<String, Object> redisTemplate; // RedisTemplate 사용
    private final ChatService chatService;

    public void publish(String channelTopic, ChatMessageDto chatMessageDto) {
        // Redis에 메시지 발행
        redisTemplate.convertAndSend(channelTopic, chatMessageDto);

        // Redis SortedSet에 값을 저장
        chatService.saveMessage(channelTopic, chatMessageDto);
    }

    // 지역 채팅방 리스트 -> 저장 안 함
    public void publishToCommunity(String channelTopic, CommunityMessage communityMessage) {
        redisTemplate.convertAndSend(channelTopic, communityMessage);
    }
}
