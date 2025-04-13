package com.snackoverflow.toolgether.domain.chat.redis

import org.springframework.data.redis.listener.ChannelTopic
import org.springframework.stereotype.Component

@Component
class TopicFactory {

    fun create(sender: String, receiver: String): ChannelTopic {

        // 정렬된 ID를 기준으로 채널 이름 생성 -> 전송 / 수신 시 순서가 바뀌어도 동일한 채팅방으로 인식할 수 있도록
        val ids = listOf(sender, receiver).sorted()
        return ChannelTopic("${ids[0]}:${ids[1]}")
    }
}