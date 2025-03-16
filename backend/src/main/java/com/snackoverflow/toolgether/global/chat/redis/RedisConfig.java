package com.snackoverflow.toolgether.global.chat.redis;

import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Configuration
public class RedisConfig {

    @Value("${spring.data.redis.host}") // application.yml에서 정의된 Redis 호스트
    private String redisHost;

    @Value("${spring.data.redis.port}") // application.yml에서 정의된 Redis 포트
    private int redisPort;

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        // RedisStandaloneConfiguration을 사용해 도커 컨테이너의 Redis와 연결 설정
        RedisStandaloneConfiguration redisConfig = new RedisStandaloneConfiguration();
        redisConfig.setHostName(redisHost);
        log.info("redis 연결 시작");
        redisConfig.setPort(redisPort);
        return new LettuceConnectionFactory(redisConfig); // Lettuce를 사용해 Connection Factory 생성
    }

    @Bean
    public RedisTemplate<String, Object> redisTemplate() {
        // RedisTemplate 설정
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        // Key와 Value를 각각 직렬화
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        redisTemplate.setValueSerializer(new Jackson2JsonRedisSerializer<>(Object.class));
        return redisTemplate;
    }

    @Bean
    public ChannelTopic gangnamTopic() {
        return new ChannelTopic("chatroom:gangnam");
    }

    @Bean
    public ChannelTopic mapoTopic() {
        return new ChannelTopic("chatroom:mapo");
    }

    @Bean
    public ChannelTopic seongdongTopic() {
        return new ChannelTopic("chatroom:seongdong");
    }

    @Bean
    public ChannelTopic nowonTopic() {
        return new ChannelTopic("chatroom:nowon");
    }

    @Bean
    public ChannelTopic gwanakTopic() {
        return new ChannelTopic("chatroom:gwanak");
    }

    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory connectionFactory) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        return container;
    }
}
