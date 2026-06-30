package com.clinmind.runtime.audit;

import java.time.Instant;
import java.util.Optional;

public record AuditLogQuery(
        Optional<String> actor,
        Optional<AuditActionType> actionType,
        Optional<AuditResourceType> resourceType,
        Optional<String> resourceId,
        Optional<Instant> from,
        Optional<Instant> to,
        int limit) {

    public AuditLogQuery {
        limit = limit <= 0 ? 50 : Math.min(limit, 200);
    }
}
