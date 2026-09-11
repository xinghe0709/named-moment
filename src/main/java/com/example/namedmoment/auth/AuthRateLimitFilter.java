package com.example.namedmoment.auth;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 30)
public class AuthRateLimitFilter extends OncePerRequestFilter {

    private static final int MAX_ATTEMPTS = 20;
    private static final long WINDOW_MILLIS = Duration.ofMinutes(10).toMillis();
    private static final String LOGIN_PATH = "/api/auth/login";
    private static final String REGISTER_PATH = "/api/auth/register";

    private final ConcurrentHashMap<String, AttemptWindow> windows = new ConcurrentHashMap<>();

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!"POST".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        String path = request.getRequestURI();
        return !LOGIN_PATH.equals(path) && !REGISTER_PATH.equals(path);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String clientKey = clientKey(request);
        AttemptWindow window = windows.computeIfAbsent(clientKey, ignored -> new AttemptWindow());
        if (!window.tryAcquire(System.currentTimeMillis())) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setHeader("Retry-After", "600");
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    "{\"code\":42901,\"message\":\"尝试次数过多，请稍后再试\",\"data\":null}");
            return;
        }
        filterChain.doFilter(request, response);
    }

    private String clientKey(HttpServletRequest request) {
        String cloudflareIp = request.getHeader("CF-Connecting-IP");
        if (StringUtils.hasText(cloudflareIp) && cloudflareIp.length() <= 64) {
            return cloudflareIp.trim();
        }
        return request.getRemoteAddr();
    }

    private static final class AttemptWindow {

        private long startedAt = System.currentTimeMillis();
        private int count;

        private synchronized boolean tryAcquire(long now) {
            if (now - startedAt >= WINDOW_MILLIS) {
                startedAt = now;
                count = 0;
            }
            count++;
            return count <= MAX_ATTEMPTS;
        }
    }
}
