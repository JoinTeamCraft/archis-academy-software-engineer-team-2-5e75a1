package tech.lokum.parkinglot.config;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApiRequestLoggingFilterTests {

    private static final Pattern UUID_PATTERN = Pattern.compile(
            "[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}"
    );

    @AfterEach
    void clearMdc() {
        MDC.clear();
    }

    @Test
    void usesRequestIdInMdcAndResponseAndRestoresPreviousContext() throws Exception {
        ApiRequestLoggingFilter filter = new ApiRequestLoggingFilter();
        MockHttpServletRequest request = apiRequest();
        request.addHeader("X-Request-Id", "  request-123  ");
        MockHttpServletResponse response = new MockHttpServletResponse();
        MDC.put("requestId", "previous-request");

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertEquals("request-123", MDC.get("requestId"));
            ((MockHttpServletResponse) servletResponse).setStatus(202);
        });

        assertEquals("request-123", response.getHeader("X-Request-Id"));
        assertEquals("previous-request", MDC.get("requestId"));
    }

    @Test
    void generatesSafeIdForInvalidHeaderAndRemovesItFromMdcAfterRequest() throws Exception {
        ApiRequestLoggingFilter filter = new ApiRequestLoggingFilter();
        MockHttpServletRequest request = apiRequest();
        request.addHeader("X-Request-Id", "bad\nrequest-id");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) -> {
            assertNotNull(MDC.get("requestId"));
            assertTrue(UUID_PATTERN.matcher(MDC.get("requestId")).matches());
        });

        String requestId = response.getHeader("X-Request-Id");
        assertNotNull(requestId);
        assertTrue(UUID_PATTERN.matcher(requestId).matches());
        assertNull(MDC.get("requestId"));
    }

    @Test
    void skipsActuatorRequests() throws Exception {
        ApiRequestLoggingFilter filter = new ApiRequestLoggingFilter();
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/actuator/health");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                assertNull(MDC.get("requestId")));

        assertFalse(response.containsHeader("X-Request-Id"));
        assertNull(MDC.get("requestId"));
    }

    @Test
    void handlesRequestUriShorterThanContextPath() throws Exception {
        ApiRequestLoggingFilter filter = new ApiRequestLoggingFilter();
        MockHttpServletRequest request = apiRequest();
        request.setContextPath("/a-long-context-path");
        MockHttpServletResponse response = new MockHttpServletResponse();

        filter.doFilter(request, response, (servletRequest, servletResponse) ->
                ((MockHttpServletResponse) servletResponse).setStatus(200));

        assertNotNull(response.getHeader("X-Request-Id"));
    }

    private MockHttpServletRequest apiRequest() {
        return new MockHttpServletRequest("GET", "/api/test");
    }
}
