package com.example.namedmoment.utils;

public final class ExplanationTextUtils {

    private ExplanationTextUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String normalize(String explanation) {
        if (explanation == null) {
            return null;
        }
        return explanation.trim()
                .replace("精准", "准确")
                .replace("完美契合", "很好地契合")
                .replace("完美对应", "很好地对应")
                .replace("完美匹配", "很好地匹配")
                .replace("完美符合", "很好地符合")
                .replace("完全契合", "契合")
                .replace("完全对应", "对应")
                .replace("完全一致", "一致")
                .replace("完全匹配", "匹配")
                .replace("完全符合", "符合");
    }
}
