package com.snackoverflow.toolgether.domain.user.dto.request;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class VerificationData {

    private String email;
    private String code;
    private boolean verified;
    private final LocalDateTime createdAt; // 생성 시간
    private final int expirationMinutes;   // 유효 시간 (분 단위)

    public VerificationData(String email, String code, boolean verified) {
        this.email = email;
        this.code = code;
        this.verified = verified;
        this.createdAt = LocalDateTime.now();
        this.expirationMinutes = 15; //15분 고정
    }

    private int attemptCount = 0; // 이메일 인증 제한

    public int incrementAttempt() {
        return ++attemptCount;
    }

    // 인증 만료 여부 체크
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(createdAt.plusMinutes(expirationMinutes));
    }
}
