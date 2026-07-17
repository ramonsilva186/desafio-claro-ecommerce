package com.claro.desafiopedidos.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-Id";
    private static final String CORRELATION_ID_KEY = "correlationId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String correlationId = resolveCorrelationId(request);
        long startTime = System.currentTimeMillis();

        MDC.put(CORRELATION_ID_KEY, correlationId);
        response.setHeader(CORRELATION_ID_HEADER, correlationId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            long durationMs = System.currentTimeMillis() - startTime;
            int status = response.getStatus();

            logByStatus(
                    status,
                    "event=http_request_completed method={} path={} status={} durationMs={} clientIp={}",
                    request.getMethod(),
                    request.getRequestURI(),
                    status,
                    durationMs,
                    request.getRemoteAddr()
            );

            MDC.remove(CORRELATION_ID_KEY);
        }
    }

    private String resolveCorrelationId(HttpServletRequest request) {
        String correlationId = request.getHeader(CORRELATION_ID_HEADER);

        if (correlationId == null || correlationId.isBlank()) {
            return UUID.randomUUID().toString();
        }

        return correlationId;
    }

    private void logByStatus(int status, String message, Object... arguments) {
        if (status >= 500) {
            log.error(message, arguments);
            return;
        }

        if (status >= 400) {
            log.warn(message, arguments);
            return;
        }

        log.info(message, arguments);
    }
}
