package com.clinmind.runtime.api;

import com.clinmind.runtime.config.ClinmindDebugApiProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 5)
public class DebugTokenFilter extends OncePerRequestFilter {

    static final String DEBUG_TOKEN_HEADER = "X-Debug-Token";

    private final ClinmindDebugApiProperties debugApiProperties;
    private final ObjectMapper objectMapper;

    public DebugTokenFilter(ClinmindDebugApiProperties debugApiProperties, ObjectMapper objectMapper) {
        this.debugApiProperties = debugApiProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        if (!debugApiProperties.isEnabled() || !debugApiProperties.isRequireDebugToken()) {
            return true;
        }
        String path = request.getRequestURI();
        return path == null || !path.startsWith("/api/v1/debug/");
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        String configuredToken = debugApiProperties.getDebugToken();
        if (configuredToken == null || configuredToken.isBlank()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "DEBUG_TOKEN_REQUIRED", "Debug token is not configured");
            return;
        }

        String providedToken = request.getHeader(DEBUG_TOKEN_HEADER);
        if (providedToken == null || providedToken.isBlank()) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "DEBUG_TOKEN_REQUIRED", "Debug token required");
            return;
        }
        if (!configuredToken.equals(providedToken)) {
            writeError(response, HttpServletResponse.SC_UNAUTHORIZED, "INVALID_DEBUG_TOKEN", "Invalid debug token");
            return;
        }

        filterChain.doFilter(request, response);
    }

    private void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(new ApiError(code, message)));
    }
}
