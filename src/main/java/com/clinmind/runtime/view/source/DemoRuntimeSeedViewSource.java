package com.clinmind.runtime.view.source;

import com.clinmind.runtime.view.clinician.dto.*;
import com.clinmind.runtime.view.common.DemoRuntimeSeedProvider;
import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import com.clinmind.runtime.view.patient.dto.*;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

@Component
public class DemoRuntimeSeedViewSource implements PatientViewSource, ClinicianViewSource {
    private final DemoRuntimeSeedProvider seed;

    public DemoRuntimeSeedViewSource(DemoRuntimeSeedProvider seed) {
        this.seed = seed;
    }

    public List<PatientSessionSummaryDto> patientSessions() {
        return seed.patientSessions().stream().map(v -> new PatientSessionSummaryDto(v.sessionId(), v.runtimeId(),
                v.status(), v.chiefComplaintSummary(), v.riskHint(), v.safeNextStep(), v.updatedAt(),
                ProjectionStatus.FALLBACK)).toList();
    }

    public Optional<PatientRuntimeViewDto> patientRuntimeView(String id) {
        return seed.patientRuntimeView(id).map(v -> new PatientRuntimeViewDto(v.sessionId(), v.runtimeId(), v.status(),
                v.safeSummary(), v.collectedFacts(), v.nextQuestions(), v.safetyNotices(), v.careNavigation(),
                v.allowedActions(), v.disclaimer(), ProjectionStatus.FALLBACK, v.missingSections()));
    }

    public Optional<PatientSafeSummaryDto> patientSafeSummary(String id) {
        return seed.patientSafeSummary(id).map(v -> new PatientSafeSummaryDto(v.sessionId(), v.runtimeId(),
                v.safeSummary(), v.safetyNotices(), v.careNavigation(), v.disclaimer(), ProjectionStatus.FALLBACK));
    }

    public List<ClinicianCaseSummaryDto> clinicianCases() {
        return seed.clinicianCases().stream().map(v -> new ClinicianCaseSummaryDto(v.caseId(), v.runtimeId(),
                v.status(), v.riskLevel(), v.chiefComplaintSummary(), v.updatedAt(), v.assignedClinician(),
                ProjectionStatus.FALLBACK)).toList();
    }

    public Optional<ClinicianCaseViewDto> clinicianCaseView(String id) {
        return seed.clinicianCaseView(id).map(v -> new ClinicianCaseViewDto(v.caseId(), v.runtimeId(), v.status(),
                v.patientSummary(), v.caseFrame(), v.inquiryTimeline(), v.ddxBoard(), v.evidencePanel(), v.riskPanel(),
                v.aiSuggestions(), fallback(v.reportDraft()), v.runtimeBoundarySummary(), ProjectionStatus.FALLBACK,
                v.missingSections()));
    }

    public Optional<ClinicianReportDraftViewDto> clinicianReportDraft(String id) {
        return seed.clinicianReportDraft(id).map(this::fallback);
    }

    private ClinicianReportDraftViewDto fallback(ClinicianReportDraftViewDto v) {
        return new ClinicianReportDraftViewDto(v.caseId(), v.runtimeId(), v.impression(), v.suggestedQuestions(),
                v.clinicianNote(), v.editable(), v.submitEnabled(), ProjectionStatus.FALLBACK);
    }
}