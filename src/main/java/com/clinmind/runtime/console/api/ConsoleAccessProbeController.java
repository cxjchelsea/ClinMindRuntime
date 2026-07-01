package com.clinmind.runtime.console.api;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.AccessPolicy;
import com.clinmind.runtime.console.access.ActorContextHolder;
import com.clinmind.runtime.console.access.ConsoleActionType;
import com.clinmind.runtime.console.access.ConsoleResourceType;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/console/access-probe")
public class ConsoleAccessProbeController {

    private final AccessPolicy accessPolicy;

    public ConsoleAccessProbeController(AccessPolicy accessPolicy) {
        this.accessPolicy = accessPolicy;
    }

    @GetMapping("/review")
    public ApiResponse<Map<String, String>> reviewProbe() {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.REVIEW,
                ConsoleResourceType.CONSOLE_REVIEW);
        return ApiResponse.ok(Map.of("status", "allowed"));
    }

    @GetMapping("/audit")
    public ApiResponse<Map<String, String>> auditProbe() {
        accessPolicy.require(
                ActorContextHolder.getRequired(),
                ConsoleActionType.READ_AUDIT,
                ConsoleResourceType.CONSOLE_AUDIT);
        return ApiResponse.ok(Map.of("status", "allowed"));
    }
}
