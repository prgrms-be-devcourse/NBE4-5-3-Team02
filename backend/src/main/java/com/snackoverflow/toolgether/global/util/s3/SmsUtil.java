package com.snackoverflow.toolgether.global.util.s3;

import com.snackoverflow.toolgether.global.exception.ServiceException;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.NurigoApp;
import net.nurigo.sdk.message.model.Message;
import net.nurigo.sdk.message.request.SingleMessageSendingRequest;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import net.nurigo.sdk.message.service.DefaultMessageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import static com.snackoverflow.toolgether.global.exception.ErrorCode.MESSAGE_SEND_FAILED;

@Slf4j
@Component
public class SmsUtil {

    @Value("${coolsms.api.key}")
    private String apiKey;

    @Value("${coolsms.api.secret}")
    private String apiSecret;

    @Value("${coolsms.api.fromnumber}")
    private String fromNumber;

    private DefaultMessageService messageService;

    @PostConstruct
    private void init() {
        // 메시지 서비스 초기화
        this.messageService = NurigoApp.INSTANCE.initialize(apiKey, apiSecret, "https://api.coolsms.co.kr");
    }

    public SingleMessageSentResponse sendSMS(String to, int verificationCode) {
        Message message = new Message();
        message.setFrom(fromNumber);  // 발신자 번호
        message.setTo(to);            // 수신자 번호
        message.setText("[♻️ Toolgether] 인증 번호: " + verificationCode + "\n 화면에 인증 번호를 입력해 주세요.");  // 메시지 내용
        log.info("메시지 전송 준비 완료:{}", message);

        try {
            // 메시지 발송 요청
            return messageService.sendOne(new SingleMessageSendingRequest(message));
        } catch (Exception e) {
            throw new ServiceException(MESSAGE_SEND_FAILED, e);
        }
    }
}
