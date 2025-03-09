package com.snackoverflow.toolgether.global.util;

import java.nio.ByteBuffer;
import java.util.Base64;
import java.util.UUID;

public class Util {
    //UUID를 Base64로 더 짧은 문자열로 표현
    public static String generateUUIDMasking() {
        try {
            UUID uuid = UUID.randomUUID();
            ByteBuffer bb = ByteBuffer.wrap(new byte[16]);
            bb.putLong(uuid.getMostSignificantBits());
            bb.putLong(uuid.getLeastSignificantBits());
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bb.array()).replace("-", "");
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
