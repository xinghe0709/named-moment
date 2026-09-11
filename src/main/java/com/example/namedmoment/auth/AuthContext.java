package com.example.namedmoment.auth;

import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class AuthContext {

    private static final ThreadLocal<AuthenticatedUser> CURRENT = new ThreadLocal<>();

    public void set(AuthenticatedUser user) {
        CURRENT.set(user);
    }

    public Optional<AuthenticatedUser> currentUser() {
        return Optional.ofNullable(CURRENT.get());
    }

    public Long requireUserId() {
        return currentUser()
                .map(AuthenticatedUser::id)
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));
    }

    public void clear() {
        CURRENT.remove();
    }
}
