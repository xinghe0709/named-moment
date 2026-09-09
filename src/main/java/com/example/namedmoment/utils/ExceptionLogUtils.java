package com.example.namedmoment.utils;

import com.example.namedmoment.constant.AppConstants;

public final class ExceptionLogUtils {

    private ExceptionLogUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String rootType(Throwable throwable) {
        Throwable root = rootCause(throwable);
        return root == null ? "unknown" : root.getClass().getSimpleName();
    }

    public static String rootMessage(Throwable throwable) {
        Throwable root = rootCause(throwable);
        if (root == null || root.getMessage() == null) {
            return "no-message";
        }
        String safeMessage = root.getMessage()
                .replace('\r', ' ')
                .replace('\n', ' ')
                .replace('\t', ' ')
                .trim();
        if (safeMessage.length() <= AppConstants.LOG_MESSAGE_MAX_LENGTH) {
            return safeMessage;
        }
        return safeMessage.substring(0, AppConstants.LOG_MESSAGE_MAX_LENGTH);
    }

    private static Throwable rootCause(Throwable throwable) {
        Throwable current = throwable;
        while (current != null && current.getCause() != null
                && current.getCause() != current) {
            current = current.getCause();
        }
        return current;
    }
}
