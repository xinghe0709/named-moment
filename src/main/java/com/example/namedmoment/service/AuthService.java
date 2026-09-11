package com.example.namedmoment.service;

import com.example.namedmoment.auth.AuthTokenService;
import com.example.namedmoment.auth.AuthenticatedUser;
import com.example.namedmoment.auth.PasswordHashService;
import com.example.namedmoment.dto.AuthRequest;
import com.example.namedmoment.dto.AuthResponse;
import com.example.namedmoment.entity.AppUser;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.mapper.AppUserMapper;
import jakarta.annotation.Resource;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
public class AuthService {

    @Resource
    private AppUserMapper appUserMapper;

    @Resource
    private PasswordHashService passwordHashService;

    @Resource
    private AuthTokenService authTokenService;

    @Transactional
    public AuthResult register(AuthRequest request) {
        String username = normalizeUsername(request.getUsername());
        if (appUserMapper.selectByUsername(username) != null) {
            throw new BusinessException(ErrorCode.USERNAME_TAKEN);
        }
        AppUser user = AppUser.builder()
                .username(username)
                .passwordHash(passwordHashService.hash(request.getPassword()))
                .createdAt(OffsetDateTime.now())
                .build();
        try {
            appUserMapper.insert(user);
        } catch (DuplicateKeyException exception) {
            throw new BusinessException(ErrorCode.USERNAME_TAKEN, exception);
        }
        return issue(user);
    }

    public AuthResult login(AuthRequest request) {
        String username = normalizeUsername(request.getUsername());
        AppUser user = appUserMapper.selectByUsername(username);
        if (user == null || !passwordHashService.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.AUTH_FAILED);
        }
        return issue(user);
    }

    public AuthResponse current(AuthenticatedUser user) {
        return toResponse(user);
    }

    private AuthResult issue(AppUser user) {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getId(), user.getUsername());
        return new AuthResult(toResponse(authenticatedUser), authTokenService.issue(authenticatedUser));
    }

    private AuthResponse toResponse(AuthenticatedUser user) {
        return AuthResponse.builder().id(user.id()).username(user.username()).build();
    }

    private String normalizeUsername(String username) {
        return username.trim().toLowerCase();
    }

    public record AuthResult(AuthResponse response, String token) {
    }
}
