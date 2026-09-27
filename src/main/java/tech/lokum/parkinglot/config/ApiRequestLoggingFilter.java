package tech.lokum.parkinglot.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.HandlerMapping;

import java.io.IOException;

@Component
public class ApiRequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(ApiRequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        long startTime = System.nanoTime();
        try {
            filterChain.doFilter(request, response);
        } finally {
            Object routePattern = request.getAttribute(HandlerMapping.BEST_MATCHING_PATTERN_ATTRIBUTE);
            String route = routePattern instanceof String pattern ? pattern : "unmatched";
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
        }
    }
}
