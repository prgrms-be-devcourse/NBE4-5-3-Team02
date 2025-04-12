package com.snackoverflow.toolgether.global.config;

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.snackoverflow.toolgether.domain.chat.redis.RedisPubSubEventPublisher
import com.snackoverflow.toolgether.domain.chat.redis.RedisPubSubEventSubscriber
import org.slf4j.Logger
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
class RedisConfig(
    @Value("\${spring.data.redis.host}") private val redisHost: String,
    @Value("\${spring.data.redis.port}") private val redisPort: Int,
    private val log: Logger
) {

    @Bean
    fun redisConnectionFactory(): RedisConnectionFactory {
        val redisConfig = RedisStandaloneConfiguration().apply {
            hostName = redisHost
            port = redisPort
        }
        log.info("Redis 연결 시작")
        return LettuceConnectionFactory(redisConfig)
    }

    @Bean
    fun redisTemplate(): RedisTemplate<String, Any> {
        return RedisTemplate<String, Any>().apply {
            connectionFactory = redisConnectionFactory()
            keySerializer = StringRedisSerializer()
            valueSerializer = Jackson2JsonRedisSerializer(Any::class.java)
        }
    }

    @Bean
    fun objectMapper(): ObjectMapper = ObjectMapper().apply {

        registerModule(KotlinModule.Builder().build())
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    @Bean
    fun messageListenerAdapter(redisSubscriber: RedisPubSubEventSubscriber): MessageListenerAdapter {
        return MessageListenerAdapter(redisSubscriber, "onMessage")
    }

    @Bean
    fun redisMessageListenerContainer(
        connectionFactory: RedisConnectionFactory,
        listenerAdapter: MessageListenerAdapter
    ): RedisMessageListenerContainer {

        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
            val topics = listOf(
                PatternTopic("${RedisPubSubEventPublisher.CHAT_EVENT_PREFIX}*"),
                PatternTopic("${RedisPubSubEventPublisher.NOTIFICATION_PREFIX}*"),
                PatternTopic("${RedisPubSubEventPublisher.COMMUNITY_EVENTS_CHANNEL}*"),
            )
            addMessageListener(listenerAdapter, topics)
        }
    }
}