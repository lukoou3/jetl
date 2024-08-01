package com.lk.jetl.sql.util;

import java.util.regex.Pattern;

public class StringUtils {

    public static String escapeLikeRegex(String pattern, char escapeChar) {
        StringBuilder out = new StringBuilder();
        int length = pattern.length();

        for (int i = 0; i < length; i++) {
            char c = pattern.charAt(i);
            if (c == escapeChar && i + 1 < length) {
                i++;
                c = pattern.charAt(i);
                if (c == '_' || c == '%') {
                    out.append(Pattern.quote(Character.toString(c)));
                } else if (c == escapeChar) {
                    out.append(Pattern.quote(Character.toString(c)));
                } else {
                    throw new IllegalArgumentException(String.format("the escape character is not allowed to precede '%s'", Character.toString(c)));
                }
            } else if (c == escapeChar) {
                throw new IllegalArgumentException("it is not allowed to end with the escape character");
            } else if (c == '_') {
                out.append(".");
            } else if (c == '%') {
                out.append(".*");
            } else {
                out.append(Pattern.quote(Character.toString(c)));
            }
        }

        return "(?s)" + out.toString(); // (?s) enables dotall mode, causing "." to match new lines
    }

    public static String trim(String srcStr, boolean leading, boolean trailing, String trimStr) {
        trimStr = trimStr == null || trimStr.isEmpty() ? " " : trimStr;
        int begin = 0, end = srcStr.length();
        if (leading) {
            while (begin < end && trimStr.indexOf(srcStr.charAt(begin)) >= 0) {
                begin++;
            }
        }
        if (trailing) {
            while (end > begin && trimStr.indexOf(srcStr.charAt(end - 1)) >= 0) {
                end--;
            }
        }
        // substring() returns self if start == 0 && end == length()
        return srcStr.substring(begin, end);
    }
}
