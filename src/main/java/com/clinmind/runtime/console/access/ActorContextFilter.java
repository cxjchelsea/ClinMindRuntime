package com.clinmind.runtime.console.access;

import com.clinmind.runtime.api.ApiError;
import com.clinmind.runtime.api.ApiResponse;
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
@Order(Ordered.HIGHEST_PRECEDENCE + 15)
public class ActorContextFilter extends OncePerRequestFilter {

    private final ActorContextResolver actorContextResolver;
    private final ObjectMapper objectMapper;

    public ActorContextFilter(ActorContextResolver actorContextResolver, ObjectMapper objectMapper) {
        this.actorContextResolver = actorContextResolver;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path == null
                || (!path.startsWith("/api/v1/debug/console/")
                && !path.startsWith("/api/v1/console/"));
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        try {
            ActorContext context = actorContextResolver.resolve(request);
            ActorContextHolder.set(context);
            actorContextResolver.bindToLegacyAuditContext(context);
            try {
                filterChain.doFilter(request, response);
            } finally {
                ActorContextHolder.clear();
            }
        } catch (InvalidDebugRoleException ex) {
            writeError(response, HttpServletResponse.SC_BAD_REQUEST, "INVALID_DEBUG_ROLE", ex.getMessage());
        }
    }

    private void writeError(HttpServletResponse response, int status, String code, String message)
            throws IOException {
        response.setStatus(status);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getWriter(), ApiResponse.fail(new ApiError(code, message)));
    }
}
