package com.example.namedmoment.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VectorUtilsTest {

    @Test
    void shouldBuildPgVectorLiteral() {
        float[] vector = new float[512];
        vector[0] = 0.25F;
        vector[511] = -0.5F;

        String literal = VectorUtils.toPgVector(vector);

        assertTrue(literal.startsWith("[0.25,"));
        assertTrue(literal.endsWith("-0.5]"));
        assertEquals(511, literal.chars().filter(value -> value == ',').count());
    }

    @Test
    void shouldRejectWrongDimension() {
        assertThrows(IllegalArgumentException.class,
                () -> VectorUtils.toPgVector(new float[511]));
    }

    @Test
    void shouldRejectNotANumber() {
        float[] vector = new float[512];
        vector[7] = Float.NaN;

        assertThrows(IllegalArgumentException.class,
                () -> VectorUtils.toPgVector(vector));
    }

    @Test
    void shouldRejectInfinity() {
        float[] vector = new float[512];
        vector[8] = Float.POSITIVE_INFINITY;

        assertThrows(IllegalArgumentException.class,
                () -> VectorUtils.toPgVector(vector));
    }
}
