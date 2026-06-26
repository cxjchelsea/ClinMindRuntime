package com.clinmind.runtime.api;

import com.clinmind.runtime.service.RuntimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Internal debug endpoints for asset usage inspection. Not intended for patient-facing clients.
 */
@RestController
@RequestMapping("/api/v1/debug/runtime")
public class DebugRuntimeController {

    private final RuntimeService runtimeService;

    public DebugRuntimeController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @GetMapping("/{runtime_id}/assets-used")
    public ApiResponse<?> getAssetsUsed(@PathVariable("runtime_id") String runtimeId) {
        return ApiResponse.ok(runtimeService.getAssetsUsed(runtimeId));
    }
}
