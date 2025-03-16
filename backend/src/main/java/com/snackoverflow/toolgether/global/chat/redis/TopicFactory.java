package com.snackoverflow.toolgether.global.chat.redis;

import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@Component
public class TopicFactory {

    public ChannelTopic create(String sender, String receiver) {
        List<String> ids = Arrays.asList(sender, receiver);
        Collections.sort(ids);
        // 정렬된 ID를 기준으로 채널 이름 생성 -> 전송 / 수신 시 순서가 바뀌어도 동일한 채팅방으로 인식할 수 있도록
        return new ChannelTopic(ids.get(0) + ":" + ids.get(1));
    }
}
