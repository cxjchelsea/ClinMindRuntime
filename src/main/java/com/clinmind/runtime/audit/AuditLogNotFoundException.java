package com.clinmind.runtime.audit;

public class AuditLogNotFoundException extends RuntimeException {

    public AuditLogNotFoundException(String auditId) {
        super("Audit log not found: " + auditId);
    }
}
