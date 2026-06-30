package com.clinmind.runtime.audit;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface AuditLogStore {

    void save(AuditLogRecord record);

    AuditLogRecord get(String auditId);

    List<AuditLogRecord> query(
            Optional<String> actor,
            Optional<AuditActionType> actionType,
            Optional<AuditResourceType> resourceType,
            Optional<String> resourceId,
            Optional<Instant> from,
            Optional<Instant> to,
            int limit);
}
