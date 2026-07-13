package com.clinmind.runtime.view.source;

import com.clinmind.runtime.view.patient.dto.PatientRuntimeViewDto;
import com.clinmind.runtime.view.patient.dto.PatientSafeSummaryDto;
import com.clinmind.runtime.view.patient.dto.PatientSessionSummaryDto;
import java.util.List;
import java.util.Optional;

public interface PatientViewSource {
    List<PatientSessionSummaryDto> patientSessions();
    Optional<PatientRuntimeViewDto> patientRuntimeView(String sessionId);
    Optional<PatientSafeSummaryDto> patientSafeSummary(String sessionId);
}