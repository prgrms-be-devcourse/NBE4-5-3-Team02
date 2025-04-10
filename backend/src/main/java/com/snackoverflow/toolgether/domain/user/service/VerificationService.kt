package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.request.VerificationData;
import com.snackoverflow.toolgether.global.exception.ServiceException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Random;

import static com.snackoverflow.toolgether.global.exception.ErrorCode.*;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class VerificationService {

    private final HttpSession session;
    private final MailService mailService;

    public static final String SESSION_KEY = "email_verification";
    private static final int MAX_ATTEMPTS = 5; // 이메일 인증 횟수 제한

    // 8자리 랜덤 인증 코드 생성 (영어 대소문자 + 숫자)
    public String createCode() {
        Random random = new Random();
        StringBuilder code = new StringBuilder();
        for (int i = 0; i < 8; i++) {
            int index = random.nextInt(3);
            switch (index) {
                case 0 -> code.append((char) (random.nextInt(26) + 97)); // 소문자
                case 1 -> code.append((char) (random.nextInt(26) + 65)); // 대문자
                case 2 -> code.append(random.nextInt(10));              // 숫자
            }
        }
        return code.toString();
    }

    // 이메일 인증을 위해 세션에 인증 정보를 저장
    @Async
    public void sendEmailWithCode(String email, HttpSession session) {
        try {
            String code = createCode();
            MimeMessage message = mailService.createMail(email, code);
            mailService.sendMail(message);

            session.setAttribute(SESSION_KEY, new VerificationData(email.trim(), code, false));
            session.setMaxInactiveInterval(60 * 15);

            log.info("세션 저장 성공: email={}, code={}", email, code);
        } catch (MessagingException e) {
            throw new ServiceException(MAIL_SEND_FAILED, e);
        }
    }

    // 인증 코드 확인 후 세션 상태 변경 verified: false -> true
    public void verifyEmail(String inputEmail, String inputCode) {
        VerificationData data = (VerificationData) session.getAttribute(SESSION_KEY);

        log.info("세션 이메일: {}, 요청 이메일: {}", data != null ? data.getEmail() : "null", inputEmail);
        log.info("세션 인증 코드: {}, 요청 인증 코드: {}", data != null ? data.getCode() : "null", inputCode);

        checkData(inputEmail, inputCode, data);

        data.setVerified(true);
        session.setAttribute(SESSION_KEY, data);
    }

    private void checkData(String inputEmail, String inputCode, VerificationData data) {
        // 세션 데이터 검증 -> 저장된 데이터가 있는가?
        if (data == null) {
            throw new ServiceException(REQUEST_NOT_FOUND); // 인증 요청이 존재하지 않음
        }

        // 세션에 저장된 이메일 일치 여부 확인
        if (!data.getEmail().trim().equals(inputEmail.trim())) {
            throw new ServiceException(NOT_VERIFIED); // 이메일 불일치
        }

        // 코드 검증
        if (!data.getCode().equals(inputCode)) {
            int remainingAttempts = MAX_ATTEMPTS - data.incrementAttempt();
            if (remainingAttempts <= 0) {
                throw new ServiceException(REQUEST_LIMIT_EXCEEDED); // 시도 횟수 초과
            }
            throw new ServiceException(CODE_MISMATCH); // 인증 코드 불일치
        }

        // 인증 만료 이전인지 검증
        if (data.isExpired()) {
            throw new ServiceException(EXPIRED); // 인증 시간 만료
        }
    }

    // 세션으로 이메일 인증 여부 확인
    public boolean isEmailVerified(String email) {
        VerificationData data = (VerificationData) session.getAttribute(SESSION_KEY);
        return data != null && data.getEmail().equals(email);
    }
}
