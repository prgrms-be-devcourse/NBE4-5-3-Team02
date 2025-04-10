package com.snackoverflow.toolgether.global.util

import mu.KotlinLogging
import java.nio.ByteBuffer
import java.util.*
import java.util.Base64.getUrlEncoder

object UUIDUtil {

    private val logger = KotlinLogging.logger {}

    // UUID를 Base64로 더 짧은 문자열로 표현
    @JvmStatic
    fun generateUUIDMasking(): String? {
        return try {
            val uuid = UUID.randomUUID()
            val bb = ByteBuffer.wrap(ByteArray(16))
            bb.putLong(uuid.mostSignificantBits)
            bb.putLong(uuid.leastSignificantBits)
            getUrlEncoder().withoutPadding().encodeToString(bb.array()).replace("-", "")
        } catch (e: Exception) {
            logger.error("Error generating UUID masking", e)
            null
        }
    }
}