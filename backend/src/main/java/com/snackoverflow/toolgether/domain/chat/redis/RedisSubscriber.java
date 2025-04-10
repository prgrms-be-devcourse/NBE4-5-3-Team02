package com.snackoverflow.toolgether.domain.chat.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.snackoverflow.toolgether.domain.chat.dto.ChatMessageDto;
import com.snackoverflow.toolgether.domain.chat.dto.CommunityMessage;
import com.snackoverflow.toolgether.domain.chat.service.ChannelSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
// Redis 서버에서 채팅방 구독자들에게 메시지를 전송
public class RedisSubscriber implements MessageListener {

    private final ObjectMapper objectMapper;
    private final ChannelSessionService channelSessionService;
    private final RedisTemplate<String, Object> redisTemplate;
    private static final String UNREAD_COUNT_KEY_PREFIX = "chat:unread:";

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String channel = new String(pattern); // 메시지가 발행된 채널(토픽) 이름
            String msg = new String(message.getBody()); // 메시지 내용
            boolean isDelivered = false;
            log.info("Redis 메시지 수신: 채널={}, 메시지={}", channel, msg);

            JsonNode parse = objectMapper.readTree(msg);
            if (parse.has("region")) {
                try {
                    CommunityMessage communityMessage = objectMapper.readValue(msg, CommunityMessage.class);
                    Set<WebSocketSession> sessions = channelSessionService.getSessions(channel);

                    for (WebSocketSession session : sessions) {
                        // 열려 있는 세션의 수 계산
                        long openSessionCount = sessions.stream().filter(WebSocketSession::isOpen).count();

                        // communityMessage에 세션 수 추가
                        communityMessage.setOpenSessionCount(openSessionCount);
                        if (session.isOpen()) {
                            session.sendMessage(new TextMessage(objectMapper.writeValueAsString(communityMessage)));
                        }
                    }
                    return;
                } catch (Exception e) {
                    log.error("메시지 처리 오류: {}", e.getMessage());
                }
            }

            // 해당 채널에 연결된 사용자들에게만 메시지 전달
            ChatMessageDto chatMessageDto = objectMapper.readValue(msg, ChatMessageDto.class);
            Set<WebSocketSession> sessions = channelSessionService.getSessions(channel);

            for (WebSocketSession session : sessions) {
                log.info("채널에 있는 세션: {}", session.getId());
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessageDto)));
                    log.info("메시지 전송 완료: 세션ID={}, 메시지={}", session.getId(), objectMapper.writeValueAsString(chatMessageDto));
                    isDelivered = true;
                }
                // 세션이 없거나 모두 닫혀 있으면 읽지 않은 메시지 카운트 증가
                if (isDelivered) {
                    incrementUnreadCount(chatMessageDto.getReceiver());
                }
            }

        } catch (JsonProcessingException e) {
            log.error("메시지 파싱 오류: {}", e.getMessage(), e);
        } catch (IOException e) {
            log.error("메시지 전송 오류: {}", e.getMessage(), e);
        } catch (Exception e) {
            log.error("알 수 없는 오류 발생: {}", e.getMessage(), e);
        }
    }

    private void incrementUnreadCount(String receiver) {
        String key = UNREAD_COUNT_KEY_PREFIX + receiver;
        redisTemplate.opsForValue().increment(key);
        log.info("읽지 않은 메시지 수 증가: 사용자={}, 키={}", receiver, key);
    }
}



