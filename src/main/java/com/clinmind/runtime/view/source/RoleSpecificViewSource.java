package com.clinmind.runtime.view.source;

import com.clinmind.runtime.view.clinician.dto.*;
import com.clinmind.runtime.view.patient.dto.*;
import java.util.List;
import java.util.Optional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Component
@Primary
public class RoleSpecificViewSource implements PatientViewSource, ClinicianViewSource {
    private final RuntimeStoreViewSource runtime;
    private final DemoRuntimeSeedViewSource fallback;

    public RoleSpecificViewSource(RuntimeStoreViewSource runtime, DemoRuntimeSeedViewSource fallback) {
        this.runtime = runtime;
        this.fallback = fallback;
    }

    public List<PatientSessionSummaryDto> patientSessions() {
        var values = runtime.patientSessions();
        return values.isEmpty() ? fallback.patientSessions() : values;
    }
    public Optional<PatientRuntimeViewDto> patientRuntimeView(String id) {
        return runtime.patientRuntimeView(id).or(() -> fallback.patientRuntimeView(id));
    }
    public Optional<PatientSafeSummaryDto> patientSafeSummary(String id) {
        return runtime.patientSafeSummary(id).or(() -> fallback.patientSafeSummary(id));
    }
    public List<ClinicianCaseSummaryDto> clinicianCases() {
        var values = runtime.clinicianCases();
        return values.isEmpty() ? fallback.clinicianCases() : values;
    }
    public Optional<ClinicianCaseViewDto> clinicianCaseView(String id) {
        return runtime.clinicianCaseView(id).or(() -> fallback.clinicianCaseView(id));
    }
    public Optional<ClinicianReportDraftViewDto> clinicianReportDraft(String id) {
        return runtime.clinicianReportDraft(id).or(() -> fallback.clinicianReportDraft(id));
    }
}