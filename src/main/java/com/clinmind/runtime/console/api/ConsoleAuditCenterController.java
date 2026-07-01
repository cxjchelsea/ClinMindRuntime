package com.clinmind.runtime.console.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.AccessPolicy;
import com.clinmind.runtime.console.access.ActorContextHolder;
import com.clinmind.runtime.console.access.ConsoleActionType;
import com.clinmind.runtime.console.access.ConsoleResourceType;
import com.clinmind.runtime.console.audit.AuditCenterService;
import com.clinmind.runtime.console.dto.AuditCenterSummaryDto;
import com.clinmind.runtime.console.dto.AuditConsoleSummaryDto;
import java.time.Instant;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/console/audit-center")
public class ConsoleAuditCenterController {

    private final AccessPolicy accessPolicy;
    private final AuditCenterService auditCenterService;

    public ConsoleAuditCenterController(AccessPolicy accessPolicy, AuditCenterService auditCenterService) {
        this.accessPolicy = accessPolicy;
        this.auditCenterService = auditCenterService;
    }

    @GetMapping("/audit-logs")
    public ApiResponse<List<AuditConsoleSummaryDto>> listAuditLogs(
            @RequestParam(value = "actor", required = false) String actor,
            @RequestParam(value = "action_type", required = false) String actionType,
            @RequestParam(value = "resource_type", required = false) String resourceType,
            @RequestParam(value = "resource_id", required = false) String resourceId,
            @RequestParam(value = "result_status", required = false) String resultStatus,
            @RequestParam(value = "from", required = false) Instant from,
            @RequestParam(value = "to", required = false) Instant to,
            @RequestParam(value = "limit", required = false) Integer limit) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.READ_AUDIT,
                ConsoleResourceType.CONSOLE_AUDIT);
        return ApiResponse.ok(auditCenterService.queryAuditLogs(
                ActorContextHolder.getRequired(),
                actor,
                actionType,
                resourceType,
                resourceId,
                resultStatus,
                from,
                to,
                limit));
    }

    @GetMapping("/audit-logs/{audit_id}")
    public ApiResponse<AuditConsoleSummaryDto> getAuditLog(@PathVariable("audit_id") String auditId) {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.READ_DETAIL,
                ConsoleResourceType.CONSOLE_AUDIT);
        return ApiResponse.ok(auditCenterService.getAuditLog(ActorContextHolder.getRequired(), auditId));
    }

    @GetMapping("/summary")
    public ApiResponse<AuditCenterSummaryDto> getSummary() {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.READ_SUMMARY,
                ConsoleResourceType.CONSOLE_AUDIT);
        return ApiResponse.ok(auditCenterService.getSummary(ActorContextHolder.getRequired()));
    }
}
