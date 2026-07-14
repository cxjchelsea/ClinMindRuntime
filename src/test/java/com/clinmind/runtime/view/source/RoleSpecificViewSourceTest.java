package com.clinmind.runtime.view.source;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.clinmind.runtime.state.*;
import com.clinmind.runtime.storage.RuntimeStore;
import com.clinmind.runtime.view.common.DemoRuntimeSeedProvider;
import com.clinmind.runtime.view.common.dto.ProjectionStatus;
import java.util.List;
import org.junit.jupiter.api.Test;

class RoleSpecificViewSourceTest {

    @Test
    void runtimeStoreDataHasPriorityOverSeedFallback() {
        RuntimeStore store = mock(RuntimeStore.class);
        RuntimeState runtime = RuntimeState.createDefault("session-real");
        runtime.setRuntimeId("runtime-real");
        runtime.setCaseFrame(new CaseFrame("real complaint", null, null, null, null, null, null, null));
        runtime.setPatientOutput(new PatientOutput());
        when(store.list(null, null, 100)).thenReturn(List.of(runtime));

        var source = source(store);

        assertThat(source.patientSessions()).singleElement().satisfies(view -> {
            assertThat(view.runtimeId()).isEqualTo("runtime-real");
            assertThat(view.projectionStatus()).isEqualTo(ProjectionStatus.PARTIAL);
        });
    }

    @Test
    void seedFallbackIsExplicitlyMarked() {
        RuntimeStore store = mock(RuntimeStore.class);
        when(store.list(null, null, 100)).thenReturn(List.of());

        var source = source(store);

        assertThat(source.patientSessions()).singleElement()
                .extracting(view -> view.projectionStatus())
                .isEqualTo(ProjectionStatus.FALLBACK);
        assertThat(source.clinicianCases()).singleElement()
                .extracting(view -> view.projectionStatus())
                .isEqualTo(ProjectionStatus.FALLBACK);
    }

    @Test
    void completeRuntimeProjectsCareNavigationAndClinicianPanels() {
        RuntimeStore store = mock(RuntimeStore.class);
        RuntimeState runtime = RuntimeState.createDefault("session-complete");
        runtime.setRuntimeId("runtime-complete");
        runtime.setCaseFrame(new CaseFrame("chest discomfort", null, null, null, null, null, null, null));
        runtime.setPatientOutput(new PatientOutput(true, "safe runtime summary", OutputLevel.O1_CONTINUE_QUESTIONING, List.of()));
        runtime.setSafetyGate(new SafetyGateResult(true, RiskLevel.MEDIUM, List.of(), "monitor",
                "seek clinician review", "safe summary only", false));
        runtime.setDecisionBoundary(new DecisionBoundaryResult(OutputLevel.O1_CONTINUE_QUESTIONING,
                false, true, "continue controlled inquiry", List.of("no diagnosis")));
        runtime.setInputHistory(List.of(new UserInput("patient runtime input")));
        runtime.setDifferentialBoard(new DifferentialDiagnosisBoard(
                List.of(new DDxCandidate("candidate", CandidateStatus.INSUFFICIENT_EVIDENCE)), "runtime"));
        runtime.setEvidenceGraph(new EvidenceGraph(List.of(new EvidenceGraphItem(
                "candidate", List.of(), List.of(), List.of(), List.of(), CandidateStatus.INSUFFICIENT_EVIDENCE,
                List.of(), List.of(), List.of(new EvidenceGraphRefEntry(
                        "evidence-1", "source-1", "chunk-1", "support", "0.8", "runtime evidence"))))));
        runtime.setClinicianReport(new ClinicianReport(true, "runtime report", "review required",
                List.of(), runtime.getEvidenceGraph(), List.of("ask duration"), List.of()));
        when(store.list(null, null, 100)).thenReturn(List.of(runtime));
        when(store.exists("runtime-complete")).thenReturn(true);
        when(store.get("runtime-complete")).thenReturn(runtime);

        var source = source(store);
        var patient = source.patientRuntimeView("runtime-complete").orElseThrow();
        var clinician = source.clinicianCaseView("runtime-complete").orElseThrow();

        assertThat(patient.careNavigation()).isNotEmpty();
        assertThat(patient.projectionStatus()).isEqualTo(ProjectionStatus.COMPLETE);
        assertThat(clinician.inquiryTimeline()).isNotEmpty();
        assertThat(clinician.evidencePanel()).isNotEmpty();
        assertThat(clinician.aiSuggestions()).isNotEmpty();
        assertThat(clinician.projectionStatus()).isEqualTo(ProjectionStatus.COMPLETE);
    }
    private RoleSpecificViewSource source(RuntimeStore store) {
        var seed = new DemoRuntimeSeedViewSource(new DemoRuntimeSeedProvider());
        return new RoleSpecificViewSource(new RuntimeStoreViewSource(store), seed);
    }
}