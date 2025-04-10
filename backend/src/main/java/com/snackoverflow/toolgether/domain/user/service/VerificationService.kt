package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.request.VerificationData
import com.snackoverflow.toolgether.global.constants.AppConstants.MAX_ATTEMPTS
import com.snackoverflow.toolgether.global.constants.AppConstants.SESSION_KEY
import com.snackoverflow.toolgether.global.exception.ErrorCode
import com.snackoverflow.toolgether.global.exception.ServiceException
import jakarta.mail.MessagingException
import jakarta.servlet.http.HttpSession
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random


@Service
@Transactional(readOnly = true)
class VerificationService(
    private val session: HttpSession,
    private val mailService: MailService
) {

    private val log = LoggerFactory.getLogger(VerificationService::class.java)

    // 8자리 랜덤 인증 코드 생성 (영어 대소문자 + 숫자)
    fun createCode(): String {
        return (0 until 8).joinToString("") {
            when (val index = Random.nextInt(3)) {
                0 -> (Random.nextInt(26) + 97).toChar() // 소문자
                1 -> (Random.nextInt(26) + 65).toChar() // 대문자
                else -> Random.nextInt(10).digitToChar() // 숫자
            }.toString()
        }
    }

    // 이메일 인증을 위해 세션에 인증 정보를 저장
    @Async
    fun sendEmailWithCode(email: String, session: HttpSession) {
        try {
            val code = createCode()
            val message = mailService.createMail(email, code)
            mailService.sendMail(message)

            session.apply {
                setAttribute(SESSION_KEY, VerificationData(email.trim(), code, false))
                maxInactiveInterval = 60 * 15  // 15분 유효기간
            }

            log.info("세션 저장 성공: email={}, code={}", email, code)
        } catch (e: MessagingException) {
            throw ServiceException(ErrorCode.MAIL_SEND_FAILED, e)
        }
    }

    // 인증 코드 확인 후 세션 상태 변경 verified: false -> true
    fun verifyEmail(inputEmail: String, inputCode: String) {
        val data = session.getAttribute(SESSION_KEY) as? VerificationData
            ?: throw ServiceException(ErrorCode.REQUEST_NOT_FOUND) // 인증 요청이 존재하지 않음

        log.info("세션 이메일: {}, 요청 이메일: {}", data.email ?: "null", inputEmail)
        log.info("세션 인증 코드: {}, 요청 인증 코드: {}", data.code ?: "null", inputCode)

        checkData(inputEmail, inputCode, data)

        data.verified = true
        session.setAttribute(SESSION_KEY, data)
    }

    private fun checkData(inputEmail: String, inputCode: String, data: VerificationData) {
        // 세션에 저장된 이메일 일치 여부 확인
        if (!data.email.trim().equals(inputEmail.trim(), ignoreCase = true)) {
            throw ServiceException(ErrorCode.NOT_VERIFIED) // 이메일 불일치
        }

        // 코드 검증
        if (data.code != inputCode) {
            val remainingAttempts = MAX_ATTEMPTS - data.incrementAttempt()
            if (remainingAttempts <= 0) {
                throw ServiceException(ErrorCode.REQUEST_LIMIT_EXCEEDED) // 시도 횟수 초과
            }
            throw ServiceException(ErrorCode.CODE_MISMATCH) // 인증 코드 불일치
        }

        // 인증 만료 이전인지 검증
        if (data.isExpired()) {
            throw ServiceException(ErrorCode.EXPIRED) // 인증 시간 만료
        }
    }

    // 세션으로 이메일 인증 여부 확인
    fun isEmailVerified(email: String): Boolean {
        val data = session.getAttribute(SESSION_KEY) as? VerificationData
        return data?.email == email && data.verified
    }
}
