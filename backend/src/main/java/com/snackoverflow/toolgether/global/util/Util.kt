package com.snackoverflow.toolgether.global.util

import java.nio.ByteBuffer
import java.util.*

object Util {
    //UUID를 Base64로 더 짧은 문자열로 표현
    @JvmStatic
    fun generateUUIDMasking(): String? {
        try {
            val uuid = UUID.randomUUID()
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(uuid.mostSignificantBits)
            bb.putLong(uuid.leastSignificantBits)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array()).replace("-", "")
        } catch (e: Exception) {
            e.printStackTrace()
            return null
        }
    }
}
