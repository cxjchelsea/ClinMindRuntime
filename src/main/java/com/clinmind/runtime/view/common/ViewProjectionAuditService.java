package com.clinmind.runtime.view.common;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ViewProjectionAuditService {

    private final AuditLogService auditLogService;

    public ViewProjectionAuditService(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
    }

    public void record(
            ActorContext context,
            AuditActionType actionType,
            String runtimeId,
            String projectionType,
            ProjectionStatus projectionStatus,
            AuditResultStatus resultStatus) {
        auditLogService.record(
                actionType,
                AuditResourceType.ROLE_SPECIFIC_VIEW,
                runtimeId,
                context.actorId(),
                resultStatus,
                Map.of(
                        "actor_id", context.actorId(),
                        "role", context.roles().toString(),
                        "request_id", context.requestId(),
                        "runtime_id", runtimeId,
                        "projection_type", projectionType,
                        "projection_status", projectionStatus.name(),
                        "result_status", resultStatus.name()));
    }
}
