package com.snackoverflow.toolgether.global.chat.service;

import com.snackoverflow.toolgether.global.chat.redis.RedisSubscriber;
import com.snackoverflow.toolgether.global.chat.redis.TopicFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class TopicSubscriptionService {

    private final RedisMessageListenerContainer redisContainer;
    private final RedisSubscriber redisSubscriber;
    private final TopicFactory topicFactory;

    public void subscribeToChatTopic(String senderId, String receiverId) {
        ChannelTopic topic = topicFactory.create(senderId, receiverId);
        log.info("채널 생성={}", topic);
        redisContainer.addMessageListener(redisSubscriber, topic);
    }

    public void subscriberToChatCommunity(ChannelTopic channelTopic) {
        log.info("채널 생성={}", channelTopic);
        redisContainer.addMessageListener(redisSubscriber, channelTopic);
    }
}