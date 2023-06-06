package com.eggtartc.airxbackend.security;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.security.SecureRandom;
import java.util.*;
import javax.crypto.spec.SecretKeySpec;

public class JwtCrypt {
    private static final Key ENCRYPTION_KEY_256;
    private static final byte[] IV_16;

    static {
        ENCRYPTION_KEY_256 = getSecureRandomKey();
        Random random = new Random(System.currentTimeMillis());
        IV_16 = new byte[16];
        for (int i = 0; i < 16; i++) {
            IV_16[i] = (byte) random.nextInt(127);
        }
    }

    private static Key getSecureRandomKey() {
        byte[] secureRandomKeyBytes = new byte[256 / 8];
        SecureRandom secureRandom = new SecureRandom();
        secureRandom.nextBytes(secureRandomKeyBytes);
        return new SecretKeySpec(secureRandomKeyBytes, "AES");
    }

    public static String encrypt(String data) {
        return Base64.getEncoder()
            .encodeToString(AESCrypt.encrypt(
                data.getBytes(), ENCRYPTION_KEY_256, IV_16
            ));
    }

    public static String decrypt(String data) {
        return new String(
            AESCrypt.decrypt(
                Base64.getDecoder().decode(data),
                ENCRYPTION_KEY_256, IV_16),
            StandardCharsets.UTF_8
        );
    }
}
