package com.example.namedmoment;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class NamedMomentApplicationTest {

    @Test
    void shouldBeSpringBootApplication() {
        SpringBootApplication annotation = NamedMomentApplication.class
                .getAnnotation(SpringBootApplication.class);

        assertNotNull(annotation);
    }
}
