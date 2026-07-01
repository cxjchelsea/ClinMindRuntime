package com.clinmind.runtime.console.access;

import com.clinmind.runtime.api.DebugActorContext;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class ActorContextResolver {

    public static final String DEBUG_ACTOR_HEADER = "X-Debug-Actor";
    public static final String DEBUG_ROLES_HEADER = "X-Debug-Roles";
    public static final String REQUEST_ID_HEADER = "X-Request-Id";

    private static final String DEFAULT_ACTOR = "system-debug";

    public ActorContext resolve(HttpServletRequest request) {
        String actorHeader = request.getHeader(DEBUG_ACTOR_HEADER);
        String actorName = actorHeader == null || actorHeader.isBlank() ? DEFAULT_ACTOR : actorHeader.trim();
        String actorId = actorName;
        List<DebugRole> roles = parseRoles(request.getHeader(DEBUG_ROLES_HEADER));
        String requestId = resolveRequestId(request);
        return new ActorContext(actorId, actorName, roles, requestId, Instant.now());
    }

    public void bindToLegacyAuditContext(ActorContext context) {
        DebugActorContext.setActor(context.actorName());
        DebugActorContext.setRequestId(context.requestId());
    }

    public List<DebugRole> parseRoles(String rolesHeader) {
        if (rolesHeader == null || rolesHeader.isBlank()) {
            return List.of(DebugRole.READ_ONLY_OBSERVER);
        }
        List<DebugRole> roles = new ArrayList<>();
        for (String token : rolesHeader.split(",")) {
            String trimmed = token.trim();
            if (trimmed.isEmpty()) {
                continue;
            }
            try {
                roles.add(DebugRole.valueOf(trimmed));
            } catch (IllegalArgumentException ex) {
                throw new InvalidDebugRoleException(trimmed);
            }
        }
        if (roles.isEmpty()) {
            return List.of(DebugRole.READ_ONLY_OBSERVER);
        }
        return List.copyOf(roles);
    }

    private static String resolveRequestId(HttpServletRequest request) {
        String requestId = request.getHeader(REQUEST_ID_HEADER);
        if (requestId == null || requestId.isBlank()) {
            return UUID.randomUUID().toString();
        }
        return requestId.trim();
    }
}
