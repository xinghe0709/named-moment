package com.example.namedmoment.utils;

public final class EmbeddingTextUtils {

    private EmbeddingTextUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String build(String meaning, String description) {
        if (meaning == null || meaning.trim().isEmpty()) {
            throw new IllegalArgumentException("meaning must not be blank");
        }
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("description must not be blank");
        }
        return "核心含义：" + meaning.trim() + "\n情境描述：" + description.trim();
    }
}
