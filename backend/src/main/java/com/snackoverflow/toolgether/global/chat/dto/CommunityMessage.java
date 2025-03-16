package com.snackoverflow.toolgether.global.chat.dto;

import lombok.Data;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Data
public class CommunityMessage {
    private String content;
    private String timestamp;
    private String senderName;
    private String region;
    @Setter
    private long openSessionCount;

    public CommunityMessage() {
        this.timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }
}
