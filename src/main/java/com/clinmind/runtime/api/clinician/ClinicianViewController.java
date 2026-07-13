package com.clinmind.runtime.api.clinician;

import com.clinmind.runtime.api.ApiResponse;
import com.clinmind.runtime.console.access.ActorContextHolder;
import com.clinmind.runtime.view.clinician.ClinicianCaseProjectionService;
import com.clinmind.runtime.view.clinician.dto.ClinicianCaseSummaryDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianCaseViewDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianReportDraftViewDto;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/clinician/cases")
public class ClinicianViewController {

    private final ClinicianCaseProjectionService projectionService;

    public ClinicianViewController(ClinicianCaseProjectionService projectionService) {
        this.projectionService = projectionService;
    }

    @GetMapping
    public ApiResponse<List<ClinicianCaseSummaryDto>> listCases() {
        return ApiResponse.ok(projectionService.listCases(ActorContextHolder.getRequired()));
    }

    @GetMapping("/{caseId}")
    public ApiResponse<ClinicianCaseViewDto> getClinicianCase(@PathVariable String caseId) {
        return ApiResponse.ok(projectionService.getCaseView(caseId, ActorContextHolder.getRequired()));
    }

    @GetMapping("/{caseId}/report-draft")
    public ApiResponse<ClinicianReportDraftViewDto> getReportDraft(@PathVariable String caseId) {
        return ApiResponse.ok(projectionService.getReportDraft(caseId, ActorContextHolder.getRequired()));
    }
}
