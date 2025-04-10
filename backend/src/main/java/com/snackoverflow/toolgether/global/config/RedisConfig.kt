package com.snackoverflow.toolgether.global.config;

import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
class RedisConfig(
        @Value("\${spring.data.redis.host}") private val redisHost: String,
        @Value("\${spring.data.redis.port}") private val redisPort: Int
) {

    private val log = LoggerFactory.getLogger(RedisConfig::class.java)

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
    fun gangnamTopic(): ChannelTopic = ChannelTopic("chatroom:gangnam")

    @Bean
    fun mapoTopic(): ChannelTopic = ChannelTopic("chatroom:mapo")

    @Bean
    fun seongdongTopic(): ChannelTopic = ChannelTopic("chatroom:seongdong")

    @Bean
    fun nowonTopic(): ChannelTopic = ChannelTopic("chatroom:nowon")

    @Bean
    fun gwanakTopic(): ChannelTopic = ChannelTopic("chatroom:gwanak")

    @Bean
    fun redisMessageListenerContainer(connectionFactory: RedisConnectionFactory): RedisMessageListenerContainer {
        return RedisMessageListenerContainer().apply {
            setConnectionFactory(connectionFactory)
        }
    }
}