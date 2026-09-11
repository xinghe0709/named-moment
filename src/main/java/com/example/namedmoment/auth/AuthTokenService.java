package com.example.namedmoment.auth;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;

@Service
public class AuthTokenService {

    public static final String COOKIE_NAME = "named_moment_token";
    public static final Duration TOKEN_TTL = Duration.ofDays(7);

    private final byte[] secret;

    public AuthTokenService(
            @Value("${auth.token-secret:change-this-local-secret-before-production}") String tokenSecret,
            @Value("${auth.require-secure-secret:false}") boolean requireSecureSecret) {
        if (requireSecureSecret && (tokenSecret.length() < 32
                || tokenSecret.startsWith("change-this-"))) {
            throw new IllegalStateException(
                    "AUTH_TOKEN_SECRET must contain at least 32 non-default characters in demo mode");
        }
        this.secret = tokenSecret.getBytes(StandardCharsets.UTF_8);
    }

    public String issue(AuthenticatedUser user) {
        long expiresAt = Instant.now().plus(TOKEN_TTL).getEpochSecond();
        String encodedUsername = encode(user.username());
        String payload = user.id() + "." + expiresAt + "." + encodedUsername;
        String encodedPayload = encode(payload);
        return encodedPayload + "." + encode(sign(encodedPayload));
    }

    public AuthenticatedUser parse(String token) {
        try {
            String[] parts = token.split("\\.", -1);
            if (parts.length != 2) {
                return null;
            }
            byte[] payloadBytes = decode(parts[0]);
            byte[] signature = decode(parts[1]);
            if (!MessageDigest.isEqual(sign(parts[0]), signature)) {
                return null;
            }
            String[] values = new String(payloadBytes, StandardCharsets.UTF_8).split("\\.", -1);
            if (values.length != 3 || Instant.now().getEpochSecond() >= Long.parseLong(values[1])) {
                return null;
            }
            return new AuthenticatedUser(Long.parseLong(values[0]),
                    new String(decode(values[2]), StandardCharsets.UTF_8));
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private byte[] sign(String value) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Token signing is unavailable", exception);
        }
    }

    private String encode(String value) {
        return encode(value.getBytes(StandardCharsets.UTF_8));
    }

    private String encode(byte[] value) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(value);
    }

    private byte[] decode(String value) {
        return Base64.getUrlDecoder().decode(value);
    }
}
