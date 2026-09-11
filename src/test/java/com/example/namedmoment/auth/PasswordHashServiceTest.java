package com.example.namedmoment.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PasswordHashServiceTest {

    private final PasswordHashService service = new PasswordHashService();

    @Test
    void hashesAreSaltedAndOnlyTheOriginalPasswordMatches() {
        String first = service.hash("correct-horse-battery");
        String second = service.hash("correct-horse-battery");

        assertNotEquals("correct-horse-battery", first);
        assertNotEquals(first, second);
        assertTrue(service.matches("correct-horse-battery", first));
        assertFalse(service.matches("wrong-password", first));
    }

    @Test
    void malformedHashDoesNotAuthenticate() {
        assertFalse(service.matches("password", "not-a-password-hash"));
    }
}
