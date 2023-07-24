package com.eggtartc.airxbackend.util;

import java.io.InputStream;
import java.security.MessageDigest;
import java.util.logging.Logger;

public class FileUtils {
    /**
     * Calculate sha256 for a file.
     */
    public static String sha256(InputStream stream) {
        byte[] buffer = new byte[8192];
        int count;
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            while ((count = stream.read(buffer)) > 0) {
                digest.update(buffer, 0, count);
            }
            stream.close();
            byte[] hash = digest.digest();

            StringBuilder builder = new StringBuilder();
            for (byte b : hash) {
                builder.append(String.format("%02x", b));
            }
            return builder.toString();
        }
        catch (Exception e) {
            // This should never happen: SHA-256 is always available
            // and the file is also guaranteed to exist.
            Logger.getLogger(FileUtils.class.getName())
                .severe(e.getMessage());
            return null;
        }
    }

    public static String encodeFilenameForHttpHeader(String filename) {
        final char[] chars = filename.toCharArray();
        final StringBuilder sb = new StringBuilder();

        for (char c : chars) {
            if (isSafe(c)) {
                sb.append(c);
            } else {
                sb.append('%');
                sb.append(Integer.toHexString(c).toUpperCase());
            }
        }

        return sb.toString();
    }

    private static boolean isSafe(char c) {
        if (c <= 127) {
            if (Character.isDigit(c) || (c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z')) {
                return true;
            }
            switch (c) {
                case '-', '.', '_', '~' -> {
                    return true;
                }
            }
        }
        return false;
    }
}
