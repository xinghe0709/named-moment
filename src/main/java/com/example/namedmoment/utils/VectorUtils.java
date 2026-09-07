package com.example.namedmoment.utils;

import com.example.namedmoment.constant.AppConstants;

public final class VectorUtils {

    private VectorUtils() {
        throw new IllegalStateException("Utility class");
    }

    public static String toPgVector(float[] vector) {
        if (vector == null || vector.length != AppConstants.EMBEDDING_DIMENSION) {
            throw new IllegalArgumentException("embedding dimension must be 512");
        }

        StringBuilder builder = new StringBuilder();
        builder.append('[');
        for (int index = 0; index < vector.length; index++) {
            float value = vector[index];
            if (Float.isNaN(value) || Float.isInfinite(value)) {
                throw new IllegalArgumentException("embedding contains an invalid number");
            }
            if (index > 0) {
                builder.append(',');
            }
            builder.append(value);
        }
        builder.append(']');
        return builder.toString();
    }
}
