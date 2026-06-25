package com.clinmind.runtime.api;

import com.clinmind.runtime.api.dto.ApiResponseMapper;
import com.clinmind.runtime.service.RuntimeExecutionResult;
import com.clinmind.runtime.service.RuntimeService;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/runtime")
public class RuntimeController {

    private final RuntimeService runtimeService;

    public RuntimeController(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @PostMapping("/start")
    public ApiResponse<?> startRuntime(@Valid @RequestBody StartRuntimeRequest request) {
        if (!StringUtils.hasText(request.input().text())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "input.text 不能为空");
        }
        RuntimeExecutionResult result = runtimeService.startRuntime(request);
        return ApiResponse.ok(
                ApiResponseMapper.toOperationResponse(result.state()),
                result.trace().getTraceId());
    }

    @PostMapping("/continue")
    public ApiResponse<?> continueRuntime(@Valid @RequestBody ContinueRuntimeRequest request) {
        if (!StringUtils.hasText(request.input().text())) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "INVALID_REQUEST", "input.text 不能为空");
        }
        RuntimeExecutionResult result = runtimeService.continueRuntime(request);
        return ApiResponse.ok(
                ApiResponseMapper.toOperationResponse(result.state()),
                result.trace().getTraceId());
    }

    @GetMapping("/{runtime_id}/status")
    public ApiResponse<?> getRuntimeStatus(@PathVariable("runtime_id") String runtimeId) {
        RuntimeState state = runtimeService.getStatus(runtimeId);
        return ApiResponse.ok(ApiResponseMapper.toStatusResponse(state));
    }

    @GetMapping("/{runtime_id}/result")
    public ApiResponse<?> getRuntimeResult(@PathVariable("runtime_id") String runtimeId) {
        RuntimeState state = runtimeService.getResult(runtimeId);
        return ApiResponse.ok(ApiResponseMapper.toResultResponse(state));
    }

    @GetMapping("/{runtime_id}/trace")
    public ApiResponse<?> getRuntimeTrace(@PathVariable("runtime_id") String runtimeId) {
        List<RuntimeTrace> traces = runtimeService.getTraces(runtimeId);
        return ApiResponse.ok(ApiResponseMapper.toTraceResponse(runtimeId, traces));
    }
}
