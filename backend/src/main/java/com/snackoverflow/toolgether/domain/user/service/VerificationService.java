package com.snackoverflow.toolgether.domain.user.service;

import com.snackoverflow.toolgether.domain.user.dto.request.VerificationData;
import com.snackoverflow.toolgether.global.exception.custom.mail.MailPreparationException;
import com.snackoverflow.toolgether.global.exception.custom.mail.VerificationException;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Random;

@Slf4j
@Service
@RequiredArgsConstructor
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
    @Async // 비동기 처리 -> 이메일 발송 시간 단축, 시스템 성능 개선
    public void sendEmailWithCode(String email, HttpSession session) {
        // 인증 코드 생성
        String code = createCode();

        try {
            // 인증 코드가 포함된 메일 생성
            MimeMessage message = mailService.createMail(email, code);

            // 메일 발송
            mailService.sendMail(message);

            // 세션에 저장
            session.setAttribute(SESSION_KEY, new VerificationData(email.trim(), code, false));
            session.setMaxInactiveInterval(60 * 15); // 15분 유효시간

            log.info("세션 저장 성공: email={}, code={}", email, code);

        } catch (MessagingException e) {
            throw new MailPreparationException("메일 구성 오류: " + e.getMessage(), e); // 이메일 형식, 제목/본문 인코딩 문제
        }
    }

    // 인증 코드 확인 후 세션 상태 변경 verified: false -> true
    public void verifyEmail(String inputEmail, String inputCode) {
        VerificationData data = (VerificationData) session.getAttribute(SESSION_KEY);

        log.info("세션 이메일: {}, 요청 이메일: {}", data.getEmail(), inputEmail);
        log.info("세션 인증 코드: {}, 요청 인증 코드: {}", data.getCode(), inputCode);

        // 세션 데이터 검증 -> 저장된 데이터가 있는가?
        if (data == null) {
            throw new VerificationException(VerificationException.ErrorType.REQUEST_NOT_FOUND, "인증 요청이 존재하지 않습니다.");
        }

        // 세션에 저장된 이메일 일치 여부 확인
        if (!data.getEmail().trim().equals(inputEmail.trim())) {
            throw new VerificationException(VerificationException.ErrorType.REQUEST_NOT_FOUND, "요청 이메일 불일치: 세션 = "
                    + data.getEmail() + " / 입력 = " + inputEmail);
        }

        // 코드 검증
        if (!data.getCode().equals(inputCode)) {
            throw new VerificationException(VerificationException.ErrorType.CODE_MISMATCH, "인증 코드 오류! 남은 시도 횟수: " + (MAX_ATTEMPTS - data.incrementAttempt()));
        }

        // 인증 만료 이전인지 검증
        if (data.isExpired()) {
            throw new VerificationException(VerificationException.ErrorType.EXPIRED, "인증 시간이 만료되었습니다. 재전송 후 시도해 주세요.");
        }

        data.setVerified(true);
        session.setAttribute(SESSION_KEY, data);
    }

    // 세션으로 이메일 인증 여부 확인
    public boolean isEmailVerified(String email) {
        VerificationData data = (VerificationData) session.getAttribute(SESSION_KEY);
        return data != null && data.getEmail().equals(email);
    }
}
