package com.snackoverflow.toolgether.domain.chat.dto

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class ChatMessageDto(
    var sender: String = "",          // 보낸 사람의 ID
    var receiver: String = "",        // 받는 사람의 ID
    var content: String = "",         // 메시지 내용
    var timeStamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), // 메시지 전송 시각
    var senderName: String = "",      // 보내는 사람 닉네임
    var receiverName: String = "",    // 받는 사람 닉네임
    var deletedSender: Boolean = false,   // 보낸 사람 기준 삭제 여부
    var deletedReceiver: Boolean = false  // 받는 사람 기준 삭제 여부
)