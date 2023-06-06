package com.eggtartc.airxbackend.util;

import java.util.*;

public class PasswordUtil {
    public static boolean isPasswordValid(String plaintextSha256Sha256, String realPasswordSalted) {
        // pass = salt + h(h(h(plaintext)) + salt)
        String saltExtracted = realPasswordSalted.substring(0, 64);
        String passwordRecovered = saltExtracted + HashUtil.sha256(plaintextSha256Sha256 + saltExtracted);
        return passwordRecovered.equals(realPasswordSalted);
    }

    public static String createPassword(String plaintext) {
        // pass = salt + h(h(h(plaintext)) + salt)
        String plaintextSha256Sha256 = HashUtil.sha256(HashUtil.sha256(plaintext));
        String salt = (UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", ""));
        return salt + HashUtil.sha256(plaintextSha256Sha256 + salt);
    }
}
