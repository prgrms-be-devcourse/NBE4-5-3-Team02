package com.snackoverflow.toolgether.global.util

import com.snackoverflow.toolgether.global.constants.AppConstants.ALGORITHM
import com.snackoverflow.toolgether.global.constants.AppConstants.KEY_SIZE
import java.security.NoSuchAlgorithmException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey

/**
 * 사용자의 위도, 경도 -> 민감한 개인 정보 값, 반드시 암호화 후에 저장해야 함
 */
object AESUtil {
    @JvmStatic
    fun createAESKey(): SecretKey {
        val keyGen = validateAlgorithm(ALGORITHM)
        keyGen.init(KEY_SIZE)
        return keyGen.generateKey()
    }

    private fun validateAlgorithm(algorithm: String): KeyGenerator {
        try {
            return KeyGenerator.getInstance(algorithm)
        } catch (e: NoSuchAlgorithmException) {
            throw IllegalStateException("AES 알고리즘이 지원되지 않습니다.", e)
        }
    }

    // 암호화
    @JvmStatic
    @Throws(Exception::class)
    fun encrypt(data: String, secretKey: SecretKey?): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey)
        val encryptedBytes = cipher.doFinal(data.toByteArray())
        return Base64.getEncoder().encodeToString(encryptedBytes)
    }

    // 복호화
    @JvmStatic
    @Throws(Exception::class)
    fun decrypt(encryptedData: String?, secretKey: SecretKey?): String {
        val cipher = Cipher.getInstance(ALGORITHM)
        cipher.init(Cipher.DECRYPT_MODE, secretKey)
        val decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData))
        return String(decryptedBytes)
    }
}
