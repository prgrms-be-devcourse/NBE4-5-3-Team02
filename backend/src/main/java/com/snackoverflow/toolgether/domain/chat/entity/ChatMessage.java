package com.snackoverflow.toolgether.domain.chat.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(indexes = {
        @Index(name = "idx_channel_timestamp", columnList = "channelName, timestamp"),
        @Index(name = "idx_sender_receiver", columnList = "senderId, receiverId")
})
public class ChatMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private String id;

    private String channelName;  // 채널 식별자
    private String senderId;     // 발신자 ID
    private String senderName;   // 발신자 이름 (denormalized)
    private String receiverId;   // 수신자 ID
    private String receiverName; // 수신자 이름 (denormalized)

    @Lob
    private String content;      // 메시지 내용

    @Column(nullable = false)
    private LocalDateTime timestamp; // 발신 시간

    private ChatMessage(String channelName, String senderId, String senderName,
                        String receiverId, String receiverName, String content) {
        this.channelName = channelName;
        this.senderId = senderId;
        this.senderName = senderName;
        this.receiverId = receiverId;
        this.receiverName = receiverName;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    // 채팅 생성 메서드
    public static ChatMessage create(String channelName, String senderId, String senderName, String receiverId, String receiverName, String content) {
    return new ChatMessage(channelName, senderId, senderName, receiverId, receiverName, content);}
}
