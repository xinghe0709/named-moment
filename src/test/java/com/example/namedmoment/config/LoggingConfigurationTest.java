package com.example.namedmoment.config;

import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;

import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertFalse;

class LoggingConfigurationTest {

    @Test
    void shouldLoadLocalEnvironmentAndIncludeRequestIdInConsoleLogs() throws Exception {
        String yaml = new ClassPathResource("application.yml")
                .getContentAsString(StandardCharsets.UTF_8);

        assertTrue(yaml.contains("optional:file:.env[.properties]"));
        assertTrue(yaml.contains("%X{requestId:-startup}"));
        assertTrue(yaml.contains("${PID:- }"));
        assertFalse(yaml.contains("%pid"));
    }
}
