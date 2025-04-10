package com.snackoverflow.toolgether.domain.user.service.service;

import com.snackoverflow.toolgether.domain.user.service.MessageService;
import com.snackoverflow.toolgether.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class MessageServiceTest {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private MessageService messageService;

    @Value("${coolsms.api.fromnumber}")
    private String fromNumber;

    private final String SMS_VERIFICATION = "sms_verification:";
    private final String RETRY_COUNT = "retry_count:";
    private final String SMS_VERIFIED = "sms_verified:";

    @BeforeEach
    void setUp() {
        // Redis 데이터 초기화
        redisTemplate.delete(SMS_VERIFICATION + fromNumber);
        redisTemplate.delete(RETRY_COUNT + fromNumber);
        redisTemplate.delete(SMS_VERIFIED + fromNumber);
    }

    /*
    @Test // 실제 sms 전송 호출 테스트 -> 과금됩니다,,, 페이지에서만 테스트 해 주세요!
    @DisplayName("SMS 전송 및 인증번호 저장")
    void sendVerificationCodeAndSave_ShouldSendSMSAndSaveCodeToRedis() {
        // given: 인증번호 생성 및 저장
        messageService.sendVerificationCodeAndSave(TEST_PHONE);

        // then: Redis에 인증번호가 저장되었는지 확인
        Integer savedCode = (Integer) redisTemplate.opsForValue().get(SMS_VERIFICATION + TEST_PHONE);
        assertThat(savedCode).isNotNull().isBetween(100000, 999999);
    }*/

    @Test
    @DisplayName("인증번호 저장 및 검증 성공")
    void verifyCode_ShouldVerifySuccessfully() {
        // 사전 조건: Redis 에 인증번호 저장
        int testCode = 871115;
        redisTemplate.opsForValue().set(SMS_VERIFICATION + fromNumber, testCode);

        // 실행: 인증번호 검증
        messageService.verifyCode(fromNumber, testCode);

        // 검증: 인증 성공 후 Redis 데이터 삭제 및 인증 여부 저장
        assertThat(redisTemplate.hasKey(SMS_VERIFICATION + fromNumber)).isFalse();
        assertThat(redisTemplate.opsForValue().get(SMS_VERIFIED + fromNumber)).isEqualTo(true);
    }


    @Test
    @DisplayName("인증 실패 - 잘못된 인증번호")
    void verifyCode_ShouldFailWithInvalidCode() {
        // given: Redis에 인증번호 저장
        int testCode = 123456;
        redisTemplate.opsForValue().set(SMS_VERIFICATION + fromNumber, testCode);

        // then: 잘못된 인증번호 입력 시 예외 발생
        ServiceException exception = assertThrows(ServiceException.class,
                () -> messageService.verifyCode(fromNumber, 111111));

        // when: 예외 메시지 검증
        assertThat(exception.getMessage()).contains("코드가 일치하지 않습니다.");

        // 인증 실패로 인증 여부 저장 안 됨, retry_count 증가
        assertThat(redisTemplate.opsForValue().get(SMS_VERIFIED + fromNumber)).isEqualTo(null);
        assertThat(redisTemplate.opsForValue().get(RETRY_COUNT + fromNumber)).isEqualTo(1);
    }

    @Test
    @DisplayName("인증 실패 - retry 3회")
    void retry_count_ShouldVerifySuccessfully() {
        // given: Redis에 인증번호 저장
        int testCode = 123456;
        redisTemplate.opsForValue().set(SMS_VERIFICATION + fromNumber, testCode);
        redisTemplate.opsForValue().set(RETRY_COUNT + fromNumber, 4);

        // then: 잘못된 인증번호 입력 시 예외 발생
        ServiceException exception = assertThrows(ServiceException.class,
                () -> messageService.verifyCode(fromNumber, 111111));

        // when: 예외 메시지 검증
        assertThat(exception.getMessage()).contains("인증 요청이 너무 많습니다.");

        // 인증 실패로 인증 여부 저장 안 됨, retry_count 증가하기 전에 로직 종료
        assertThat(redisTemplate.opsForValue().get(RETRY_COUNT + fromNumber)).isEqualTo(4);
    }

}