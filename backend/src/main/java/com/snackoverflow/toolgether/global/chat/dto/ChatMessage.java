package com.snackoverflow.toolgether.global.chat.dto;
import lombok.Data;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@Data
public class ChatMessage {
    private String sender;   // 보낸 사람의 ID
    private String receiver; // 받는 사람의 ID
    private String content;  // 메시지 내용
    private String timeStamp; // 메시지 전송 시각
    private String senderName; // 보내는 사람 닉네임
    private String receiverName; // 받는 사람 닉네임

    public ChatMessage() {
        this.timeStamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}