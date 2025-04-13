package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.global.exception.ErrorCode
import com.snackoverflow.toolgether.global.exception.ServiceException;
import com.snackoverflow.toolgether.global.util.s3.SmsUtil;
import org.slf4j.LoggerFactory
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration
import java.util.concurrent.TimeUnit

/**
 * 테스트 실행 시 환경 변수가 제대로 읽히지 않는 문제가 있음 (하드 코딩한 이유)
 * 실제로 테스트 진행하면 문제 없이 반영됨
 */

@Service
@Transactional(readOnly = true)
class MessageService(
    private val smsUtil: SmsUtil,
    private val redisTemplate: RedisTemplate<String, Any>
) {
    companion object {
        const val SMS_VERIFICATION: String = "sms_verification:"
        const val RETRY_COUNT: String = "retry_count:"
        const val SMS_VERIFIED: String = "sms_verified:"
    }
    // 메시지 인증 관련
    private val log = LoggerFactory.getLogger(MessageService::class.java)

    fun sendVerificationCodeAndSave(phoneNumber: String) {
        // 6자리 랜덤 인증 번호 생성
        val verificationCode = (100_000..999_999).random()

        // 인증 번호 전송
        val response = smsUtil.sendSMS(phoneNumber, verificationCode)
        log.info("SMS 전송 완료: {}", response)
        // 발송된 인증 번호를 Redis 에 저장하고 일정 시간 후 자동으로 만료되도록 설정 (Redis -> 원자성 보장)
        saveVerificationCode(phoneNumber, verificationCode)
    }

    // 인증 번호 검증
    fun verifyCode(phoneNumber: String, code: Int) {
        val savedCode = getVerificationCode(phoneNumber)
        log.info("저장된 코드: {}", savedCode)

        when {
            savedCode == null -> throw ServiceException(ErrorCode.REQUEST_NOT_FOUND)
            savedCode != code -> handleRetry(phoneNumber)
        }

        // 성공했을 경우
        deleteKeys(phoneNumber)
        redisTemplate.opsForValue().set("sms_verified:$phoneNumber", true, 10, TimeUnit.MINUTES)
    }

    private fun handleRetry(phoneNumber: String) {
        val retryCount = getRetryCount(phoneNumber)

        when {
            retryCount > 3 -> throw ServiceException(ErrorCode.REQUEST_LIMIT_EXCEEDED)
            else -> {
                addRetryCount(phoneNumber, retryCount)
                throw ServiceException(ErrorCode.CODE_MISMATCH)
            }
        }
    }

    // Redis 유틸리티 확장 함수 -> prefix + String 을 더해서 key 값을 만든다
    private fun String.withPrefix(prefix: String) = prefix + this

    /**
     *  Redis 에 저장되지 않았다면 인증이 실패했거나 인증을 받지 않았거나 10분 이상이 지난 경우
     *  키가 존재하면 true, 존재하지 않으면 false 반환
     */
    fun isVerified(phoneNumber: String): Boolean =
        redisTemplate.hasKey("sms_verified:$phoneNumber") // 키가 존재하는지 확인

    private fun saveVerificationCode(phoneNumber: String, code: Int) {
        redisTemplate.opsForValue().set(
            "sms_verification:$phoneNumber",
            code,
            Duration.ofMinutes(5) // 5분 후 만료 설정
        )
    }

    private fun deleteKeys(phoneNumber: String) {
        redisTemplate.delete("sms_verification:$phoneNumber")
        redisTemplate.delete("retry_count:$phoneNumber")
       /* listOf("sms_verification:", "retry_count:").forEach { prefix ->
            redisTemplate.delete(prefix.withPrefix(phoneNumber))
        }*/
    }

    // 휴대폰 인증 추가 시도 횟수 증가
    private fun addRetryCount(phoneNumber: String, retryCount: Int) {
        redisTemplate.opsForValue().apply {
            increment("retry_count:$phoneNumber").also {
                set(("retry_count:$phoneNumber"), it!!, Duration.ofMinutes(5))
            }
        }
    }

    // 추가 시도 횟수 반환
    private fun getRetryCount(phoneNumber: String): Int =
        redisTemplate.opsForValue()
            .get("retry_count:$phoneNumber")
            ?.toString() // Redis 에 저장된 값을 String 으로 변환
            ?.toIntOrNull() // 숫자 변환, 숫자 형식이 아니면 null 반환
            ?: 0 // 기본값: 모든 단계에서 null 발생 시 0을 반환

    fun getVerificationCode(phoneNumber: String): Int {

        return redisTemplate.opsForValue().get("sms_verification:$phoneNumber") as Int
    }

    private fun generateVerificationCode(): Int = (100_000..999_999).random() // 6자리 숫자 생성
}
