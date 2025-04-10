package com.snackoverflow.toolgether.global.constants;

import java.util.*

object AppConstants {
    // 토큰
    const val REFRESH_TOKEN = "refresh_token"
    const val REMEMBER_ME_TOKEN = "remember_me_token"

    // 위도, 경도 암호화 유틸 클래스
    const val ALGORITHM = "AES"
    const val KEY_SIZE = 256
    val BASE64_ENCODER = Base64.getEncoder()
    val BASE64_DECODER = Base64.getDecoder()

    // 채팅 관련
    const val PERSONAL_CHAT_PREFIX = "chat:"

    // 메시지 전송 관련
    const val MESSAGE_TEMPLATE = "[♻️ Toolgether] 인증 번호: %d\n 화면에 인증 번호를 입력해 주세요."

    const val SESSION_KEY = "email_verification"
    const val MAX_ATTEMPTS = 5 // 이메일 인증 횟수 제한

}
