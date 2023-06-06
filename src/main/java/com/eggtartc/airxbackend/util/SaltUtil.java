package com.eggtartc.airxbackend.util;

public class SaltUtil {
    public static String saltFromCurrentTimestamp() {
        return String.valueOf((System.currentTimeMillis() / 1000 / 10));
    }

    public static String calculateSaltForLogin(
        String plaintextSha256Sha256,
        int identityPrincipal
    ) {
        return HashUtil.sha256(
            plaintextSha256Sha256
                + saltFromCurrentTimestamp()
                + identityPrincipal
        );
    }
}
