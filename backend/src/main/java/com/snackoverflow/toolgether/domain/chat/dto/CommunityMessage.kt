package com.snackoverflow.toolgether.domain.chat.dto

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

data class CommunityMessage(
    var content: String = "",          // 메시지 내용
    var timestamp: String = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), // 메시지 전송 시각
    var senderName: String = "",       // 보내는 사람 닉네임
    var region: String = "",           // 지역 정보
    var openSessionCount: Long = 0     // 열려 있는 세션 수
)
