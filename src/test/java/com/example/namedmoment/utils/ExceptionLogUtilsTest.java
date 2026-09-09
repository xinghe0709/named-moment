package com.example.namedmoment.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExceptionLogUtilsTest {

    @Test
    void shouldExposeRootCauseWithoutAllowingMultilineLogInjection() {
        IllegalStateException root = new IllegalStateException("provider rejected\nrequest");
        RuntimeException wrapper = new RuntimeException("wrapper", root);

        assertEquals("IllegalStateException", ExceptionLogUtils.rootType(wrapper));
        assertEquals("provider rejected request", ExceptionLogUtils.rootMessage(wrapper));
    }

    @Test
    void shouldLimitVeryLongProviderMessages() {
        String longMessage = "x".repeat(800);

        String result = ExceptionLogUtils.rootMessage(new RuntimeException(longMessage));

        assertTrue(result.length() <= 500);
    }
}
