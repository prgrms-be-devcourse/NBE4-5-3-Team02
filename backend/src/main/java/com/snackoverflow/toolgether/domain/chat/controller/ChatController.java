package com.snackoverflow.toolgether.domain.chat.controller;

import com.snackoverflow.toolgether.domain.chat.dto.ChatMessageDto;
import com.snackoverflow.toolgether.domain.chat.service.ChatService;
import com.snackoverflow.toolgether.global.dto.RsData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatController {

    private final ChatService chatService;

    // 사용자가 포함된 전체 채팅 채널을 반환하는 엔드포인트
    @GetMapping("/channels")
    public RsData<?> getChannels(@RequestParam("userId") String userId) {
        List<String> channels = chatService.getChannels(userId);
        if (channels.isEmpty()) {
            return new RsData<>("404-1",
                    "사용자가 포함된 채널이 없습니다.",
                    null);
        }
        for (String channel : channels) {
            log.info("채널 목록 조회:{}", channel);
        }
        return new RsData<>(
                "200-1",
                "사용자가 포함된 채널 목록 조회 성공",
                channels
        );
    }

    /**
     * 특정 채널의 모든 채팅 내역을 반환하는 엔드포인트
     *
     * @param channelName 채널 이름
     * @return 채팅 메시지 목록 (JSON 형태)
     */
    @GetMapping("/history")
    public RsData<?> getChatHistory(@RequestParam("channelName") String channelName,
                                    @RequestParam("userId") String userId) {
        List<ChatMessageDto> chatHistory = chatService.getChatHistory(channelName, userId);
        log.info("저장한 채팅 - 채널:{}, 내역:{}", channelName, chatHistory);
        return new RsData<>(
                "200-1",
                "채팅 내역 불러오기 성공",
                chatHistory
        );
    }

    // 채팅방 삭제
    @DeleteMapping("/delete")
    public RsData<?> deleteChat(@RequestParam("channel") String channel,
                                @RequestParam("userId") String userId) {
        chatService.deleteChannelMessages(channel, userId);
        return new RsData<>(
                "201-1",
                "채널 삭제 완료",
                true
        );
    }

    // 읽지 않은 메시지 카운트 조회
    @GetMapping("/unread-count")
    public RsData<Integer> getUnreadCount(@RequestParam("userId") String userId) {
        Integer unreadCount = chatService.getUnreadCount(userId);
        log.info("읽지 않은 메시지의 개수: {}", unreadCount);
        return new RsData<>(
                "200-1",
                "읽지 않은 메시지의 수 조회 성공",
                unreadCount
        );
    }
}
