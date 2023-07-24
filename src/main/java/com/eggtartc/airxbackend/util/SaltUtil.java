package com.eggtartc.airxbackend.util;

public class SaltUtil {
    public static String saltFromCurrentTimestamp() {
        return String.valueOf((System.currentTimeMillis() / 1000 / 10));
    }

    public static String calculateSaltForLogin(String hhp, int uid) {
        return HashUtil.sha256(hhp + saltFromCurrentTimestamp() + uid);
    }
}
// hhp = h(h(p))
// h(hhp + t + uid)
