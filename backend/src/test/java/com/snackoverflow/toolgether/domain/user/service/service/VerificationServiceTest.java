package com.snackoverflow.toolgether.domain.user.service.service;

import com.snackoverflow.toolgether.domain.user.dto.request.VerificationData;
import com.snackoverflow.toolgether.domain.user.service.MailService;
import com.snackoverflow.toolgether.domain.user.service.VerificationService;
import com.snackoverflow.toolgether.global.exception.ServiceException;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class VerificationServiceTest {

    @Autowired private
    VerificationService verificationService;

    @Autowired
    private MailService mailService;

    @Autowired
    private HttpSession session;

    private String TEST_EMAIL = "suunn001@gmail.com";
    private final String SESSION_KEY = "email_verification";

    @BeforeEach
    void beforeEach() {
        // 세션 초기화
        session.invalidate();
    }

/*    @Test
    @DisplayName("이메일 인증 코드 발송 및 세션 저장")
    void sendEmailWithCode_ShouldSaveVerificationDataToSession() throws InterruptedException {
        // 실행: 이메일 인증 코드 발송 -> 테스트 시에는 @Async 끄고 해야 합니다
        verificationService.sendEmailWithCode(TEST_EMAIL, session);

        // 작업 완료 대기 (간단한 대기)
        Thread.sleep(1000);

        // 검증: 세션에 데이터가 저장되었는지 확인
        VerificationData data = (VerificationData) session.getAttribute(SESSION_KEY);
        assertThat(data.getEmail()).isEqualTo(TEST_EMAIL);
        assertThat(data.getCode()).isNotEmpty();
        assertThat(data.isVerified()).isFalse();
    }*/

    @Test
    @DisplayName("이메일 인증 성공")
    void verifyEmail_ShouldVerifySuccessfully() {
        // given: 세션에 임의의 값 저장
        session.setAttribute(SESSION_KEY, new VerificationData(TEST_EMAIL, "123456", false));

        // 세션에 저장된 인증 코드 가져오기
        VerificationData data = (VerificationData) session.getAttribute(SESSION_KEY);
        String verificationCode = data.getCode();

        // when: 이메일 인증 검증
        verificationService.verifyEmail(TEST_EMAIL, verificationCode);

        // then: 세션 데이터가 인증 완료 상태로 변경되었는지 확인
        VerificationData updatedData = (VerificationData) session.getAttribute(SESSION_KEY);
        assertThat(updatedData.getVerified()).isTrue();
    }

    @Test
    @DisplayName("이메일 인증 실패 - 잘못된 코드")
    void verifyEmail_ShouldFailWithInvalidCode() {
        // given: 세션에 임의의 값 저장
        session.setAttribute(SESSION_KEY, new VerificationData(TEST_EMAIL, "123456", false));

        // when & then: 잘못된 인증 코드 입력 시 예외 발생
        ServiceException exception = assertThrows(ServiceException.class,
                () -> verificationService.verifyEmail(TEST_EMAIL, "wrong-code"));

        assertThat(exception.getCode()).isEqualTo("400-2");
        assertThat(exception.getMessage()).contains("코드가 일치하지 않습니다.");
    }

/*    @Test
    @DisplayName("이메일 인증 실패 - 만료된 세션")
    void verifyEmail_ShouldFailWhenSessionExpired() throws InterruptedException {
        // given: 세션에 임의의 값 저장
        session.setAttribute(SESSION_KEY, new VerificationData(TEST_EMAIL, "123456", false));

        // 세션 만료 시뮬레이션 (15분 대기)
        Thread.sleep(1000 * 60 * 15); // 15분 대기

        // when & then: 만료된 세션으로 인증 시도 시 예외 발생
        ServiceException exception = assertThrows(ServiceException.class,
                () -> verificationService.verifyEmail(TEST_EMAIL, "some-code"));

        assertThat(exception.getCode()).isEqualTo("400-3");
        assertThat(exception.getMessage()).contains("만료된 인증 정보입니다.");
    }*/

    @Test
    @DisplayName("이메일 불일치로 인증 실패")
    void verifyEmail_ShouldFailWhenEmailsDoNotMatch() {
        // given: 세션에 임의의 값 저장
        session.setAttribute(SESSION_KEY, new VerificationData(TEST_EMAIL, "123456", false));

        // when & then: 다른 이메일로 인증 시도 시 예외 발생
        ServiceException exception = assertThrows(ServiceException.class,
                () -> verificationService.verifyEmail("wrong@example.com", "some-code"));

        assertThat(exception.getCode()).isEqualTo("400-4");
        assertThat(exception.getMessage()).contains("이메일이 인증되지 않았습니다.");
    }
}