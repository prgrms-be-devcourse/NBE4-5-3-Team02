package com.snackoverflow.toolgether.domain.post.entity.enums;

import lombok.Getter;

import java.util.Arrays;

@Getter
public enum RecurrenceDays {
    NONE(0, "반복 없음"),
    MONDAY(1, "월요일"),
    TUESDAY(2, "화요일"),
    WEDNESDAY(3, "수요일"),
    THURSDAY(4, "목요일"),
    FRIDAY(5, "금요일"),
    SATURDAY(6, "토요일"),
    SUNDAY(7, "일요일");

    private final int code;
    private final String day;

    RecurrenceDays(int code, String day) {
        this.code = code;
        this.day = day;
    }

    /**
     * 코드로 Enum 조회
     */
    public static RecurrenceDays fromCode(int code) {
        return Arrays.stream(values())
                .filter(day -> day.code == code)
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 코드: " + code));
    }

    /**
     * 요일로 Enum 조회
     */
    public static RecurrenceDays fromDay(String day) {
        return Arrays.stream(values())
                .filter(d -> d.day.equals(day))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("잘못된 요일: " + day));
    }
}
