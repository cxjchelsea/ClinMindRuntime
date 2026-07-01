package com.clinmind.runtime.console.access;

import java.time.Instant;
import java.util.List;

public record ActorContext(
        String actorId,
        String actorName,
        List<DebugRole> roles,
        String requestId,
        Instant resolvedAt
) {
    public ActorContext {
        if (actorId == null || actorId.isBlank()) {
            throw new IllegalArgumentException("actorId must not be blank");
        }
        if (actorName == null || actorName.isBlank()) {
            throw new IllegalArgumentException("actorName must not be blank");
        }
        if (roles == null || roles.isEmpty()) {
            roles = List.of(DebugRole.READ_ONLY_OBSERVER);
        } else {
            roles = List.copyOf(roles);
        }
        if (requestId == null || requestId.isBlank()) {
            throw new IllegalArgumentException("requestId must not be blank");
        }
        if (resolvedAt == null) {
            throw new IllegalArgumentException("resolvedAt must not be null");
        }
    }

    public boolean hasRole(DebugRole role) {
        return roles.contains(role);
    }

    public boolean isSystemAdmin() {
        return hasRole(DebugRole.SYSTEM_ADMIN);
    }
}
