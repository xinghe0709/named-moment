package com.example.namedmoment.utils;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmbeddingTextUtilsTest {

    @Test
    void shouldBuildStableEmbeddingText() {
        String text = EmbeddingTextUtils.build(
                "想回去却无法回去",
                "旧物触发异乡往事");

        assertEquals("核心含义：想回去却无法回去\n情境描述：旧物触发异乡往事", text);
    }
}
