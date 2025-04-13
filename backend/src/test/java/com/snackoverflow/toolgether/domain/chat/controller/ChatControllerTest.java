package com.snackoverflow.toolgether.domain.chat.controller;

import com.snackoverflow.toolgether.domain.chat.dto.ChatMessage;
import com.snackoverflow.toolgether.domain.chat.service.ChatService;
import com.snackoverflow.toolgether.global.filter.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("local")
@AutoConfigureMockMvc
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ChatService chatService;

    private final String testUserId1 = "testUser123";
    private final String testUserId2 = "testUser456";
    private final String CHAT_EVENT_PREFIX = "chat:event:";
    private final String channelName = testUserId1 + ":" + testUserId2;
    private final String redisKey = CHAT_EVENT_PREFIX + testUserId1 + ":" + testUserId2;

    @BeforeEach
    void setup() {
        // Redis 채널 데이터를 초기화하여 테스트 환경을 격리
        redisTemplate.delete(redisKey);

        // 메시지 생성
        ChatMessage message1 = new ChatMessage(
                testUserId1, testUserId2,
                "Hello!", "2025-04-13 12:00:00",
                "USER1NAME", "USER2NAME",
                true, false);

        ChatMessage message2 = new ChatMessage(testUserId2, testUserId1,
                "안녕!", "2025-04-13 12:01:30",
                "USER2NAME", "USER1NAME",
                false, true);

        chatService.saveMessage(channelName, message1);
        chatService.saveMessage(channelName, message2);

        CustomUserDetails userDetails = new CustomUserDetails(1L, "test@example.com");
        SecurityContext context = SecurityContextHolder.getContext();
        context.setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                )
        );
    }

    @Test
    @DisplayName("사용자 채널 목록 조회 - 성공 케이스")
    void testGetChannels() throws Exception {

        mockMvc.perform(get("/api/chat/channels")
                        .param("userId", testUserId1)
                        .header("X-Test-Auth", "test@example.com")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("사용자가 포함된 채널 목록 조회 성공"))
                .andExpect(jsonPath("$.data").isArray());
    }

    @Test
    @DisplayName("채팅 내역 조회 - 성공 케이스")
    void testGetChatHistory() throws Exception {
        mockMvc.perform(get("/api/chat/history")
                        .param("channelName", channelName)
                        .header("X-Test-Auth", "test@example.com")
                        .param("userId", testUserId1)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200-1"))
                .andExpect(jsonPath("$.msg").value("채팅 내역 불러오기 성공"))
                .andExpect(jsonPath("$.data").isArray());
    }
}