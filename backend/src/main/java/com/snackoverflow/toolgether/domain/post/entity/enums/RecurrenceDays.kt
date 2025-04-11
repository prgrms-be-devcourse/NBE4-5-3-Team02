package com.snackoverflow.toolgether.domain.post.entity.enums

import lombok.Getter
import java.util.*

enum class RecurrenceDays(private val code: Int, private val day: String) {
    NONE(0, "반복 없음"),
    MONDAY(1, "월요일"),
    TUESDAY(2, "화요일"),
    WEDNESDAY(3, "수요일"),
    THURSDAY(4, "목요일"),
    FRIDAY(5, "금요일"),
    SATURDAY(6, "토요일"),
    SUNDAY(7, "일요일");

    fun getCode(): Int {
        return code;
    }

    companion object {
        /**
         * 코드로 Enum 조회
         */
        fun fromCode(code: Int): RecurrenceDays {
            return Arrays.stream(entries.toTypedArray())
                .filter { day: RecurrenceDays -> day.code == code }
                .findFirst()
                .orElseThrow {
                    IllegalArgumentException(
                        "잘못된 코드: $code"
                    )
                }
        }

        /**
         * 요일로 Enum 조회
         */
        fun fromDay(day: String): RecurrenceDays {
            return Arrays.stream(entries.toTypedArray())
                .filter { d: RecurrenceDays -> d.day == day }
                .findFirst()
                .orElseThrow {
                    IllegalArgumentException(
                        "잘못된 요일: $day"
                    )
                }
        }
    }
}
