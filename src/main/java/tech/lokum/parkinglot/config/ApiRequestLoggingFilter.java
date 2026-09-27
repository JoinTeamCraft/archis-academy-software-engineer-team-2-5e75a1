package tech.lokum.parkinglot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;
import java.util.Locale;
import java.util.UUID;
import java.util.regex.Pattern;

@Component
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);
    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private static final String REQUEST_ID_MDC_KEY = "requestId";
    private static final Pattern SAFE_REQUEST_ID = Pattern.compile("[A-Za-z0-9._-]{1,128}");

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = getPathWithinApplication(request);
        String lowerCasePath = path.toLowerCase(Locale.ROOT);
        return path.equals("/actuator") || path.startsWith("/actuator/")
                || path.equals("/swagger-ui.html") || path.startsWith("/swagger-ui/")
                || path.startsWith("/v3/api-docs") || path.startsWith("/webjars/")
                || lowerCasePath.equals("/favicon.ico")
                || lowerCasePath.endsWith(".css") || lowerCasePath.endsWith(".js")
                || lowerCasePath.endsWith(".png") || lowerCasePath.endsWith(".jpg")
                || lowerCasePath.endsWith(".jpeg") || lowerCasePath.endsWith(".gif")
                || lowerCasePath.endsWith(".svg") || lowerCasePath.endsWith(".ico")
                || lowerCasePath.endsWith(".woff") || lowerCasePath.endsWith(".woff2");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String requestId = resolveRequestId(request.getHeader(REQUEST_ID_HEADER));
        String previousRequestId = MDC.get(REQUEST_ID_MDC_KEY);
        MDC.put(REQUEST_ID_MDC_KEY, requestId);

        long startTime = System.nanoTime();
        try {
            response.setHeader(REQUEST_ID_HEADER, requestId);
            filterChain.doFilter(request, response);
        } finally {
            try {
                Object routePattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
                String route = routePattern instanceof String pattern ? pattern : request.getRequestURI();
                long durationMillis = (System.nanoTime() - startTime) / 1_000_000;
                int status = response.getStatus();

                if (status >= 500) {
                    LOG.error("API request failed: method={}, route={}, status={}, durationMs={}",
                            request.getMethod(), route, status, durationMillis);
                } else if (status >= 400) {
                    LOG.warn("API request rejected: method={}, route={}, status={}, durationMs={}",
                            request.getMethod(), route, status, durationMillis);
                } else {
                    LOG.info("API request completed: method={}, route={}, status={}, durationMs={}",
                            request.getMethod(), route, status, durationMillis);
                }
            } finally {
                if (previousRequestId == null) {
                    MDC.remove(REQUEST_ID_MDC_KEY);
                } else {
                    MDC.put(REQUEST_ID_MDC_KEY, previousRequestId);
                }
            }
        }
    }

    private String resolveRequestId(String requestId) {
        if (requestId != null) {
            String normalizedRequestId = requestId.strip();
            if (SAFE_REQUEST_ID.matcher(normalizedRequestId).matches()) {
                return normalizedRequestId;
            }
        }
        return UUID.randomUUID().toString();
    }

    private String getPathWithinApplication(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        if (requestUri == null || requestUri.isEmpty()) {
            return "";
        }
        if (contextPath != null && !contextPath.isEmpty() && requestUri.startsWith(contextPath)) {
            return requestUri.substring(contextPath.length());
        }
        return requestUri;
    }
}
