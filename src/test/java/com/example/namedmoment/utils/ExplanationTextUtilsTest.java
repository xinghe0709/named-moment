package com.example.namedmoment.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ExplanationTextUtilsTest {

    @Test
    void shouldToneDownAbsoluteMatchWording() {
        assertEquals("它准确捕捉了细节，并很好地契合用户体验。",
                ExplanationTextUtils.normalize(
                        " 它精准捕捉了细节，并完美契合用户体验。 "));
    }
}
