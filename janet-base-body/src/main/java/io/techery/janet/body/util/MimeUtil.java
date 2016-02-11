package io.techery.janet.body.util;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.regex.Pattern.CASE_INSENSITIVE;

public final class MimeUtil {

    private static final Pattern CHARSET = Pattern.compile("\\Wcharset=([^\\s;]+)", CASE_INSENSITIVE);

    @Deprecated
    public static String parseCharset(String mimeType) {
        return parseCharset(mimeType, "UTF-8");
    }

    public static String parseCharset(String mimeType, String defaultCharset) {
        Matcher match = CHARSET.matcher(mimeType);
        if (match.find()) {
            return match.group(1).replaceAll("[\"\\\\]", "");
        }
        return defaultCharset;
    }

    private MimeUtil() {
        // No instances.
    }
}
