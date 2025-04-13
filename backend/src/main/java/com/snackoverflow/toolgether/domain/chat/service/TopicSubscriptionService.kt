package com.snackoverflow.toolgether.domain.chat.service

import com.snackoverflow.toolgether.domain.chat.redis.RedisPubSubEventSubscriber
import com.snackoverflow.toolgether.domain.chat.redis.TopicFactory
import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer
import org.springframework.stereotype.Service

@Service
class TopicSubscriptionService(
    private val container: RedisMessageListenerContainer,
    private val topicFactory: TopicFactory,
    private val redisPubSubEventSubscriber: RedisPubSubEventSubscriber
) {

    // 1:1 채팅
    fun subscribeToChatTopic(senderId: String, receiverId: String) {
        val topic = topicFactory.create(senderId, receiverId)
        container.addMessageListener(redisPubSubEventSubscriber, topic)
    }

    // 커뮤니티 채팅
    fun subscriberToChatCommunity(channelTopic: ChannelTopic) {
        container.addMessageListener(redisPubSubEventSubscriber, channelTopic)
    }
}