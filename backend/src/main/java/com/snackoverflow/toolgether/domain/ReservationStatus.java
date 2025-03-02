package com.snackoverflow.toolgether.domain;

public enum ReservationStatus {
    REQUESTED,           // 요청됨
    APPROVED,            // 승인됨
    IN_PROGRESS,         // 진행 중
    REJECTED,            // 거절됨
    DONE,                // 정상 완료됨
    FAILED_OWNER_ISSUE,  // 소유자 문제로 실패함
    FAILED_RENTER_ISSUE  // 대여자 문제로 실패함
}
