package com.eggtartc.airxbackend.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
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
}
