package com.clinmind.runtime.state;

import java.util.List;

public record ClinicianReport(
        boolean allowed,
        String caseSummary,
        String safetySummary,
        List<DDxCandidate> ddxSummary,
        EvidenceGraph evidenceSummary,
        List<String> recommendedQuestions,
        List<String> recommendedTests
) {
    public ClinicianReport {
        ddxSummary = ddxSummary == null ? List.of() : List.copyOf(ddxSummary);
        recommendedQuestions = recommendedQuestions == null ? List.of() : List.copyOf(recommendedQuestions);
        recommendedTests = recommendedTests == null ? List.of() : List.copyOf(recommendedTests);
    }

    public ClinicianReport() {
        this(false, null, null, List.of(), null, List.of(), List.of());
    }
}
