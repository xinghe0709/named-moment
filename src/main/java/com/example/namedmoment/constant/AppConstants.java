package com.example.namedmoment.constant;

public final class AppConstants {

    public static final int INPUT_MIN_LENGTH = 5;
    public static final int INPUT_MAX_LENGTH = 2000;
    public static final int VECTOR_RECALL_LIMIT = 10;
    public static final int MATCH_RESULT_LIMIT = 3;
    public static final int MATCH_SCORE_MIN = 0;
    public static final int MATCH_SCORE_MAX = 100;
    public static final int TOOL_QUERY_LIMIT = 20;
    public static final int TOOL_KEYWORD_LIMIT = 5;
    public static final int TOOL_KEYWORD_MIN_LENGTH = 2;
    public static final int TOOL_KEYWORD_MAX_LENGTH = 20;
    public static final int EMBEDDING_DIMENSION = 512;
    public static final int EMBEDDING_BATCH_SIZE = 20;
    public static final int AI_SEMANTIC_MAX_ATTEMPTS = 2;
    public static final int REQUEST_ID_MAX_LENGTH = 64;
    public static final int LOG_MESSAGE_MAX_LENGTH = 500;
    public static final double MIN_VECTOR_SCORE = 0.50D;
    public static final String REQUEST_ID_HEADER = "X-Request-Id";
    public static final String REQUEST_ID_MDC_KEY = "requestId";
    public static final String API_PATH_PREFIX = "/api/";

    private AppConstants() {
        throw new IllegalStateException("Utility class");
    }
}
