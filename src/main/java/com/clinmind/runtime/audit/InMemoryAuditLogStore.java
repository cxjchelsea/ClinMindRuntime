package com.clinmind.runtime.audit;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryAuditLogStore implements AuditLogStore {

    private final Map<String, AuditLogRecord> records = new ConcurrentHashMap<>();

    @Override
    public void save(AuditLogRecord record) {
        records.put(record.auditId(), record);
    }

    @Override
    public AuditLogRecord get(String auditId) {
        AuditLogRecord record = records.get(auditId);
        if (record == null) {
            throw new AuditLogNotFoundException(auditId);
        }
        return record;
    }

    @Override
    public List<AuditLogRecord> query(
            java.util.Optional<String> actor,
            java.util.Optional<AuditActionType> actionType,
            java.util.Optional<AuditResourceType> resourceType,
            java.util.Optional<String> resourceId,
            java.util.Optional<java.time.Instant> from,
            java.util.Optional<java.time.Instant> to,
            int limit) {
        return records.values().stream()
                .filter(record -> actor.isEmpty() || actor.get().equals(record.actor()))
                .filter(record -> actionType.isEmpty() || actionType.get() == record.actionType())
                .filter(record -> resourceType.isEmpty() || resourceType.get() == record.resourceType())
                .filter(record -> resourceId.isEmpty() || resourceId.get().equals(record.resourceId()))
                .filter(record -> from.isEmpty() || !record.createdAt().isBefore(from.get()))
                .filter(record -> to.isEmpty() || !record.createdAt().isAfter(to.get()))
                .sorted(Comparator.comparing(AuditLogRecord::createdAt).reversed())
                .limit(Math.max(limit, 1))
                .toList();
    }
}
