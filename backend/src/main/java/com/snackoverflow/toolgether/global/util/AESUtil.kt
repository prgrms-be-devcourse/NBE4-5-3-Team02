package com.snackoverflow.toolgether.global.util;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import static com.snackoverflow.toolgether.global.constants.AppConstants.ALGORITHM;
import static com.snackoverflow.toolgether.global.constants.AppConstants.KEY_SIZE;

/**
 * 사용자의 위도, 경도 -> 민감한 개인 정보 값, 반드시 암호화 후에 저장해야 함
 */
public class AESUtil {

    public static SecretKey createAESKey() {
        KeyGenerator keyGen = validateAlgorithm(ALGORITHM);
        keyGen.init(KEY_SIZE);
        return keyGen.generateKey();
    }

    private static KeyGenerator validateAlgorithm(String algorithm) {
        try {
            return KeyGenerator.getInstance(algorithm);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("AES 알고리즘이 지원되지 않습니다.", e);
        }
    }

    // 암호화
    public static String encrypt(String data, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        byte[] encryptedBytes = cipher.doFinal(data.getBytes());
        return Base64.getEncoder().encodeToString(encryptedBytes);
    }

    // 복호화
    public static String decrypt(String encryptedData, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance(ALGORITHM);
        cipher.init(Cipher.DECRYPT_MODE, secretKey);
        byte[] decryptedBytes = cipher.doFinal(Base64.getDecoder().decode(encryptedData));
        return new String(decryptedBytes);
    }
}
