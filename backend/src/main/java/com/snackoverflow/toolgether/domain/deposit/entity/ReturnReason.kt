package com.snackoverflow.toolgether.domain.deposit.entity;

public enum ReturnReason {
    NONE,               // 반환 이전
    NORMAL_COMPLETION,  // 정상 완료
    DAMAGE_REPORTED,    // 물건 훼손 발생
    ITEM_LOSS,          // 물건 분실 발생
    REJECTED,           // 요청 거절
    UNRESPONSIVE_RENTER // 대여자의 무응답
}
