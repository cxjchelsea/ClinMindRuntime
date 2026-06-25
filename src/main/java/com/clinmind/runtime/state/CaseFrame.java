package com.clinmind.runtime.state;

import java.util.ArrayList;
import java.util.List;

public record CaseFrame(
        String chiefComplaint,
        PatientProfile patientProfile,
        List<SymptomItem> symptoms,
        List<String> pastHistory,
        List<String> medicationHistory,
        List<String> examinationResults,
        List<String> missingSlots,
        List<String> conflictingSlots
) {
    public CaseFrame {
        patientProfile = patientProfile == null ? new PatientProfile() : patientProfile;
        symptoms = symptoms == null ? List.of() : List.copyOf(symptoms);
        pastHistory = pastHistory == null ? List.of() : List.copyOf(pastHistory);
        medicationHistory = medicationHistory == null ? List.of() : List.copyOf(medicationHistory);
        examinationResults = examinationResults == null ? List.of() : List.copyOf(examinationResults);
        missingSlots = missingSlots == null ? List.of() : List.copyOf(missingSlots);
        conflictingSlots = conflictingSlots == null ? List.of() : List.copyOf(conflictingSlots);
    }

    public CaseFrame() {
        this(null, new PatientProfile(), List.of(), List.of(), List.of(), List.of(), List.of(), List.of());
    }
}
