package com.clinmind.runtime.api.patient;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.ActorContextHolder;
import com.clinmind.runtime.view.patient.PatientViewProjectionService;
import com.clinmind.runtime.view.patient.dto.PatientRuntimeViewDto;
import com.clinmind.runtime.view.patient.dto.PatientSafeSummaryDto;
import com.clinmind.runtime.view.patient.dto.PatientSessionSummaryDto;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/patient/sessions")
public class PatientViewController {

    private final PatientViewProjectionService projectionService;

    public PatientViewController(PatientViewProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @GetMapping
    public ApiResponse<List<PatientSessionSummaryDto>> listMySessions() {
        return ApiResponse.ok(projectionService.listSessions(ActorContextHolder.getRequired()));
    }

    @GetMapping("/{sessionId}")
    public ApiResponse<PatientRuntimeViewDto> getPatientRuntimeView(@PathVariable String sessionId) {
        return ApiResponse.ok(projectionService.getRuntimeView(sessionId, ActorContextHolder.getRequired()));
    }

    @GetMapping("/{sessionId}/summary")
    public ApiResponse<PatientSafeSummaryDto> getPatientSafeSummary(@PathVariable String sessionId) {
        return ApiResponse.ok(projectionService.getSafeSummary(sessionId, ActorContextHolder.getRequired()));
    }
}
