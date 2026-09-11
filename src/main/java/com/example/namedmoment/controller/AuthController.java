package com.example.namedmoment.controller;

import com.example.namedmoment.auth.AuthContext;
import com.example.namedmoment.auth.AuthTokenService;
import com.example.namedmoment.auth.AuthenticatedUser;
import com.example.namedmoment.dto.AuthRequest;
import com.example.namedmoment.dto.AuthResponse;
import com.example.namedmoment.dto.Result;
import com.example.namedmoment.enums.ErrorCode;
import com.example.namedmoment.exception.BusinessException;
import com.example.namedmoment.service.AuthService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Resource
    private AuthService authService;

    @Resource
    private AuthContext authContext;

    @Value("${auth.cookie-secure:false}")
    private boolean cookieSecure;

    @PostMapping("/register")
    public Result<AuthResponse> register(@Valid @RequestBody AuthRequest request,
                                         HttpServletResponse response) {
        AuthService.AuthResult result = authService.register(request);
        writeTokenCookie(response, result.token(), AuthTokenService.TOKEN_TTL);
        return Result.success(result.response());
    }

    @PostMapping("/login")
    public Result<AuthResponse> login(@Valid @RequestBody AuthRequest request,
                                      HttpServletResponse response) {
        AuthService.AuthResult result = authService.login(request);
        writeTokenCookie(response, result.token(), AuthTokenService.TOKEN_TTL);
        return Result.success(result.response());
    }

    @PostMapping("/logout")
    public Result<Void> logout(HttpServletResponse response) {
        writeTokenCookie(response, "", Duration.ZERO);
        return Result.success(null);
    }

    @GetMapping("/me")
    public Result<AuthResponse> me() {
        AuthenticatedUser user = authContext.currentUser()
                .orElseThrow(() -> new BusinessException(ErrorCode.AUTH_REQUIRED));
        return Result.success(authService.current(user));
    }

    private void writeTokenCookie(HttpServletResponse response, String token, Duration maxAge) {
        ResponseCookie cookie = ResponseCookie.from(AuthTokenService.COOKIE_NAME, token)
                .httpOnly(true)
                .secure(cookieSecure)
                .sameSite("Lax")
                .path("/")
                .maxAge(maxAge)
                .build();
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }
}
