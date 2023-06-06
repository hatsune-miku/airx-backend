package com.eggtartc.airxbackend.util;

import org.springframework.util.StringUtils;
import java.util.function.Function;

public class NumericUtil {
    private static <T> T parseOrDefault(String str, Function<String, T> parser, T defaultValue) {
        String s = str.trim();
        if (!StringUtils.hasLength(s)) {
            return defaultValue;
        }
        try {
            return parser.apply(s);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    public static double parseDoubleOrDefault(String str, double defaultValue) {
        return parseOrDefault(str, Double::parseDouble, defaultValue);
    }

    public static int parseIntOrDefault(String str, int defaultValue) {
        return parseOrDefault(str, Integer::parseInt, defaultValue);
    }

    public static int parseIntBooleanOrDefault(String str, int defaultValue) {
        return parseBooleanOrDefault(str, defaultValue == 1) ? 1 : 0;
    }

    public static boolean parseBooleanOrDefault(String str, boolean defaultValue) {
        return parseOrDefault(str, v -> {
            String vLower = v.trim().toLowerCase();
            if (vLower.equals("true") || vLower.equals("1")) {
                return true;
            }
            if (vLower.equals("false") || vLower.equals("0")) {
                return false;
            }
            throw new NumberFormatException();
        }, defaultValue);
    }
}
