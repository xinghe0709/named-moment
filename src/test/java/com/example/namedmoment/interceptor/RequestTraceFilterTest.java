package com.example.namedmoment.interceptor;

import jakarta.servlet.FilterChain;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class RequestTraceFilterTest {

    @Test
    void shouldCreateExposeAndClearRequestIdForApiRequests() throws Exception {
        RequestTraceFilter filter = new RequestTraceFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("POST", "/api/emotions/match");
        MockHttpServletResponse response = new MockHttpServletResponse();
        AtomicReference<String> requestIdInsideChain = new AtomicReference<String>();
        FilterChain chain = (servletRequest, servletResponse) ->
                requestIdInsideChain.set(MDC.get("requestId"));

        filter.doFilter(request, response, chain);

        assertNotNull(requestIdInsideChain.get());
        assertEquals(requestIdInsideChain.get(), response.getHeader("X-Request-Id"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void shouldReuseValidCallerRequestIdAndIgnoreStaticResources() throws Exception {
        RequestTraceFilter filter = new RequestTraceFilter();
        MockHttpServletRequest apiRequest = new MockHttpServletRequest("GET", "/api/emotions/records");
        apiRequest.addHeader("X-Request-Id", "client-request-123");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(apiRequest, response, (request, servletResponse) ->
                assertEquals("client-request-123", MDC.get("requestId")));

        assertEquals("client-request-123", response.getHeader("X-Request-Id"));
        assertFalse(filter.shouldNotFilter(apiRequest));
        assertEquals(true, filter.shouldNotFilter(
                new MockHttpServletRequest("GET", "/css/styles.css")));
    }
}
