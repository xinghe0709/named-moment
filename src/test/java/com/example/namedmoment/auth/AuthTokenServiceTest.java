package com.example.namedmoment.auth;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertNull;

class AuthTokenServiceTest {

    private final AuthTokenService service = new AuthTokenService("test-secret", false);

    @Test
    void issuedTokenRoundTripsTheUserIdentity() {
        AuthenticatedUser original = new AuthenticatedUser(7L, "quiet_reader");

        AuthenticatedUser parsed = service.parse(service.issue(original));

        assertEquals(original, parsed);
    }

    @Test
    void tamperedTokenIsRejected() {
        String token = service.issue(new AuthenticatedUser(7L, "quiet_reader"));
        String tampered = token.substring(0, token.length() - 1)
                + (token.endsWith("a") ? "b" : "a");

        assertNull(service.parse(tampered));
    }

    @Test
    void demoModeRejectsTheDefaultWeakSecret() {
        assertThrows(IllegalStateException.class,
                () -> new AuthTokenService("change-this-local-secret-before-production", true));
    }
}
