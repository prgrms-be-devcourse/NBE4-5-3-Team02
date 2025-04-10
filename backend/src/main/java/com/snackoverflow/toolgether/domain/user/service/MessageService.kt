package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.util.s3.SmsUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.nurigo.sdk.message.response.SingleMessageSentResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;
import java.util.Random;
import java.util.concurrent.TimeUnit;

import static com.snackoverflow.toolgether.global.exception.ErrorCode.*;


@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MessageService {

    public static final String SMS_VERIFICATION = "sms_verification:";
    public static final String RETRY_COUNT = "retry_count:";
    private static final String SMS_VERIFIED = "sms_verified:";

    private final SmsUtil smsUtil;
    private final RedisTemplate<String, Object> redisTemplate;

    public void sendVerificationCodeAndSave(String phoneNumber) {
        // 6자리 랜덤 인증 번호 생성
         int verificationCode = generateVerificationCode();

        // SMS 전송
        SingleMessageSentResponse response = smsUtil.sendSMS(phoneNumber, verificationCode);
        log.info("SMS 전송:{}", response);

        // 발송된 인증 번호를 Redis 에 저장하고 일정 시간 후 자동으로 만료되도록 설정 (@Transactional 불필요)
        saveVerificationCode(phoneNumber, verificationCode);
    }

    // 인증 번호 검증
    public void verifyCode(String phoneNumber, int code) {
        Integer savedCode = getVerificationCode(phoneNumber);

        if (String.valueOf(savedCode) == null) {
            throw new ServiceException(REQUEST_NOT_FOUND);
        } else if (savedCode != code) { // 인증 번호가 일치하지 않을 경우 재시도 횟수를 증가
            int retryCount = getRetryCount(phoneNumber);

            if (retryCount > 3) { // 재시도 횟수는 3회로 제한
                // 특정 전화번호는 고유한 값 -> race condition 발생 x, 원자적 연산 도입 x
                throw new ServiceException(REQUEST_LIMIT_EXCEEDED);
            } else {
                addRetryCount(phoneNumber, retryCount);
                throw new ServiceException(CODE_MISMATCH);
            }
        }
        // 성공 시 데이터 삭제
        deleteKeys(phoneNumber);

        // 성공 시 인증 여부를 Redis 에 저장 [phoneNumer, true]-> 10 분간만 지속
        String key = SMS_VERIFIED + phoneNumber;
        redisTemplate.opsForValue().set(key, true, 10, TimeUnit.MINUTES);
    }

    // Redis 에 저장되지 않았다면 인증이 실패했거나 인증을 받지 않았거나 10분 이상이 지난 경우
    public Boolean isVerified(String phoneNumber) {
        // Redis에 키가 존재하는지 확인
        // 키가 존재하면 true, 존재하지 않으면 false 반환
        return redisTemplate.hasKey(SMS_VERIFIED + phoneNumber);
    }

    private void saveVerificationCode(String phoneNumber, int verificationCode) {
        redisTemplate.opsForValue().set(SMS_VERIFICATION + phoneNumber, verificationCode, 5, TimeUnit.MINUTES); // 5분 후 만료 설정
    }

    private void deleteKeys(String phoneNumber) {
        redisTemplate.delete(SMS_VERIFICATION + phoneNumber);
        redisTemplate.delete(RETRY_COUNT + phoneNumber);
    }

    private void addRetryCount(String phoneNumber, int retryCount) {
        Long count = redisTemplate.opsForValue().increment(RETRY_COUNT + phoneNumber);
        redisTemplate.opsForValue().set(RETRY_COUNT + phoneNumber, count, 5, TimeUnit.MINUTES);
    }

    private int getRetryCount(String phoneNumber) {
        return redisTemplate.opsForValue().get(RETRY_COUNT + phoneNumber) != null
                ? Integer.parseInt(Objects.requireNonNull(redisTemplate.opsForValue().get(RETRY_COUNT + phoneNumber)).toString())
                : 0;
    }

    public Integer getVerificationCode(String phoneNumber) {
        String key = "sms_verification:" + phoneNumber;
        return (Integer) redisTemplate.opsForValue().get(key);
    }

    private Integer generateVerificationCode() {
        Random random = new Random();
        return random.nextInt(1000000);  // 6자리 숫자 생성
    }
}
