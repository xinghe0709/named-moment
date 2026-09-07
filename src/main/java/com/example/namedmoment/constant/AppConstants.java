package com.example.namedmoment.constant;

public final class AppConstants {

    public static final int INPUT_MIN_LENGTH = 5;
    public static final int INPUT_MAX_LENGTH = 2000;
    public static final int VECTOR_RECALL_LIMIT = 10;
    public static final int MATCH_RESULT_LIMIT = 3;
    public static final int TOOL_QUERY_LIMIT = 20;
    public static final int TOOL_KEYWORD_LIMIT = 5;
    public static final int TOOL_KEYWORD_MIN_LENGTH = 2;
    public static final int TOOL_KEYWORD_MAX_LENGTH = 20;
    public static final int EMBEDDING_DIMENSION = 512;
    public static final int EMBEDDING_BATCH_SIZE = 20;
    public static final double MIN_VECTOR_SCORE = 0.50D;

    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }
}
