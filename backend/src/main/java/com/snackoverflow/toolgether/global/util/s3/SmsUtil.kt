package com.snackoverflow.toolgether.global.util.s3

import com.snackoverflow.toolgether.global.exception.ErrorCode
import com.snackoverflow.toolgether.global.exception.ServiceException
import jakarta.annotation.PostConstruct
import mu.KotlinLogging
import net.nurigo.sdk.NurigoApp
import net.nurigo.sdk.NurigoApp.initialize
import net.nurigo.sdk.message.model.Message
import net.nurigo.sdk.message.request.SingleMessageSendingRequest
import net.nurigo.sdk.message.response.SingleMessageSentResponse
import net.nurigo.sdk.message.service.DefaultMessageService
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class SmsUtil {
    private val log = KotlinLogging.logger {}

    @Value("\${coolsms.api.key}")
    private val apiKey: String? = null

    @Value("\${coolsms.api.secret}")
    private val apiSecret: String? = null

    @Value("\${coolsms.api.fromnumber}")
    private val fromNumber: String? = null

    private var messageService: DefaultMessageService? = null

    @PostConstruct
    private fun init() {
        // 메시지 서비스 초기화
        this.messageService = initialize(apiKey!!, apiSecret!!, "https://api.coolsms.co.kr")
    }

    fun sendSMS(to: String?, verificationCode: Int): SingleMessageSentResponse? {
        val message = Message()
        message.from = fromNumber // 발신자 번호
        message.to = to // 수신자 번호
        message.text = "[♻️ Toolgether] 인증 번호: $verificationCode\n 화면에 인증 번호를 입력해 주세요." // 메시지 내용
        log.info("메시지 전송 준비 완료: $message")

        try {
            // 메시지 발송 요청
            return messageService!!.sendOne(SingleMessageSendingRequest(message))
        } catch (e: Exception) {
            throw ServiceException(ErrorCode.MESSAGE_SEND_FAILED, e)
        }
    }
}
