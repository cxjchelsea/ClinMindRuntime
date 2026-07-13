package com.clinmind.runtime.view.source;

import com.clinmind.runtime.view.clinician.dto.ClinicianCaseSummaryDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianCaseViewDto;
import com.clinmind.runtime.view.clinician.dto.ClinicianReportDraftViewDto;
import java.util.List;
import java.util.Optional;

public interface ClinicianViewSource {
    List<ClinicianCaseSummaryDto> clinicianCases();
    Optional<ClinicianCaseViewDto> clinicianCaseView(String caseId);
    Optional<ClinicianReportDraftViewDto> clinicianReportDraft(String caseId);
}