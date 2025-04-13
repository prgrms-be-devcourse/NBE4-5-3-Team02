package com.snackoverflow.toolgether.domain.chat;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackoverflow.toolgether.domain.chat.dto.ChatMessageDto;
import com.snackoverflow.toolgether.domain.chat.dto.CommunityMessage;
import com.snackoverflow.toolgether.domain.chat.redis.RedisPublisher;
import com.snackoverflow.toolgether.domain.chat.redis.TopicFactory;
import com.snackoverflow.toolgether.domain.chat.service.ChannelSessionService;
import com.snackoverflow.toolgether.domain.chat.service.TopicSubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;

/**
 * 1:1 채팅 라우팅: 사용자 간 고유 채널 생성 ex. room:user1:user2
 * 특정 채널로 메시지 라우팅 처리
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final RedisPublisher redisPublisher; // Redis 메시지 발행자
    private final ObjectMapper objectMapper; // JSON 처리
    private final TopicFactory topicFactory; // 채널(토픽) 생성
    private final TopicSubscriptionService topicSubscriptionService; // 채널 구독 용도
    private final ChannelSessionService channelSessionService;
    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws IOException {
        // 세션에서 userId 가져오기
        String userId = (String) session.getAttributes().get("userId");
        log.info("클라이언트 연결 성공: {}, userId: {}", session.getId(), userId);
        
        // 읽지 않은 메시지를 초기화
        String key = "chat:unread:" + userId;
        Integer unreadCount = (Integer) redisTemplate.opsForValue().get(key);
        if (unreadCount != null && unreadCount > 0) {
            session.sendMessage(new TextMessage("읽지 않은 메시지가 " + unreadCount + "개 있습니다."));
            redisTemplate.delete(key); // 초기화
        }
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // 메시지 처리 (Redis Pub/Sub으로 발행)
        log.info("message 변환 전: {}", message.getPayload());
        String payload = message.getPayload();

        // JSON 데이터를 먼저 파싱하여 메시지 유형 확인
        JsonNode jsonNode = objectMapper.readTree(payload);
        log.info("jsonNode 확인:{}", jsonNode);

        if (jsonNode.has("region")) { // 단체 채팅 메시지인지 확인
            CommunityMessage communityMessage = objectMapper.readValue(message.getPayload(), CommunityMessage.class);

            // 지역에 따라 적절한 채널 선택
            String region = communityMessage.getRegion(); // 예: "강남구", "마포구"
            log.info("지역 확인: {}", region);

            ChannelTopic channelTopic = new ChannelTopic("chatroom:" + region);
            topicSubscriptionService.subscriberToChatCommunity(channelTopic);
            log.info("생성된 채널 확인: {}", channelTopic);

            // WebSocket 세션 등록 및 메시지 발행
            channelSessionService.addSession(channelTopic.getTopic(), session);
            log.info("채널명:{}, 등록된 세션:{}", channelTopic.getTopic(), session);

            redisPublisher.publishToCommunity(channelTopic.getTopic(), communityMessage);
            log.info("Redis에 메시지 발행: {}", communityMessage);
        } else {
            ChatMessageDto chatMessageDto = objectMapper.readValue(message.getPayload(), ChatMessageDto.class);
            log.info("message json에서 변환:{}", chatMessageDto);

            // 토픽 생성 후 구독 설정
            ChannelTopic channelTopic = topicFactory.create(chatMessageDto.getSender(), chatMessageDto.getReceiver());
            topicSubscriptionService.subscribeToChatTopic(chatMessageDto.getSender(), chatMessageDto.getReceiver());

            // WebSocket 세션을 해당 채널에 등록
            channelSessionService.addSession(channelTopic.getTopic(), session);
            log.info("채널명:{}, 등록된 세션:{}", channelTopic.getTopic(), session);

            // Redis로 메시지 발행
            redisPublisher.publish(channelTopic.getTopic(), chatMessageDto); // Redis로 메시지 발행
            log.info("Redis에 메시지 발행: {}", chatMessageDto);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        // 연결 종료 시 모든 채널에서 해당 세션 제거
        channelSessionService.removeSession(session);
    }
}

