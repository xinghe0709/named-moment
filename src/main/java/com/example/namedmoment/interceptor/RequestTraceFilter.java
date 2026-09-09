package com.example.namedmoment.interceptor;

import com.example.namedmoment.constant.AppConstants;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RequestTraceFilter extends OncePerRequestFilter {

    private static final Pattern SAFE_REQUEST_ID =
            Pattern.compile("[A-Za-z0-9._-]{8," + AppConstants.REQUEST_ID_MAX_LENGTH + "}");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(AppConstants.API_PATH_PREFIX);
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        String requestId = resolveRequestId(request);
        response.setHeader(AppConstants.REQUEST_ID_HEADER, requestId);
        long startedAt = System.nanoTime();

        try (MDC.MDCCloseable ignored =
                     MDC.putCloseable(AppConstants.REQUEST_ID_MDC_KEY, requestId)) {
            log.info("event=http-request outcome=started method={} path={}",
                    request.getMethod(), request.getRequestURI());
            try {
                filterChain.doFilter(request, response);
            } finally {
                long durationMs = TimeUnit.NANOSECONDS.toMillis(
                        System.nanoTime() - startedAt);
                log.info("event=http-request outcome=completed method={} path={} "
                                + "httpStatus={} durationMs={}",
                        request.getMethod(), request.getRequestURI(),
                        response.getStatus(), durationMs);
            }
        }
    }

    private String resolveRequestId(HttpServletRequest request) {
        String callerRequestId = request.getHeader(AppConstants.REQUEST_ID_HEADER);
        if (StringUtils.hasText(callerRequestId)
                && SAFE_REQUEST_ID.matcher(callerRequestId).matches()) {
            return callerRequestId;
        }
        return UUID.randomUUID().toString();
    }
}
