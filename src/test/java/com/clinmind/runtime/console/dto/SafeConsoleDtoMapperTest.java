package com.clinmind.runtime.console.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogRecord;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import com.clinmind.runtime.candidate.review.CandidateKind;
import com.clinmind.runtime.candidate.review.CandidateReviewDecision;
import com.clinmind.runtime.candidate.review.CandidateReviewRecord;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.UserInput;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SafeConsoleDtoMapperTest {

    private SafeConsoleDtoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new SafeConsoleDtoMapper();
    }

    @Test
    void runtimeSummaryExcludesPatientFacingFields() {
        RuntimeState state = RuntimeState.createDefault("session_001");
        state.setRuntimeStatus(RuntimeStatus.SAFETY_GATE_TRIGGERED);
        state.getInputHistory().add(new UserInput("胸口闷，活动后更明显"));

        RuntimeConsoleSummaryDto summary = mapper.toRuntimeSummary(state);
        RuntimeConsoleDetailDto detail = mapper.toRuntimeDetail(state);

        assertThat(summary.runtimeId()).isEqualTo(state.getRuntimeId());
        assertThat(summary.runtimeStatus()).isEqualTo("SAFETY_GATE_TRIGGERED");
        assertThat(detail.safetyGateTriggered()).isTrue();
    }

    @Test
    void evaluationDetailIncludesAggregatesWithoutRawSnapshots() {
        EvaluationRun run = sampleEvaluationRun();

        EvaluationConsoleDetailDto detail = mapper.toEvaluationDetail(run);

        assertThat(detail.passRate()).isEqualTo(1.0);
        assertThat(detail.itemSummaries()).hasSize(1);
        assertThat(detail.itemSummaries().get(0).caseId()).isEqualTo("chest_pain_high_risk_001");
    }

    @Test
    void trainingCandidateDetailExcludesRawInput() {
        CandidateConsoleDetailDto detail =
                mapper.toTrainingCandidateDetail(CandidateTestFixtures.sampleTrainingExampleCandidate());

        assertThat(detail.candidateKind()).isEqualTo("TRAINING_EXAMPLE_CANDIDATE");
        assertThat(detail.policyMetadata()).doesNotContainKey("input");
        assertThat(detail.policyMetadata()).doesNotContainKey("text");
    }

    @Test
    void auditSummaryStripsSensitiveMetadata() {
        var record = sampleAuditRecord();
        var withSensitive = new AuditLogRecord(
                record.auditId(),
                record.requestId(),
                record.actor(),
                record.actionType(),
                record.resourceType(),
                record.resourceId(),
                record.resultStatus(),
                record.createdAt(),
                Map.of(
                        "mode", "in-memory",
                        "patient_output", "secret patient text",
                        "input", Map.of("text", "raw input")));

        AuditConsoleSummaryDto summary = mapper.toAuditSummary(withSensitive);

        assertThat(summary.metadataSummary()).containsEntry("mode", "in-memory");
        assertThat(summary.metadataSummary()).doesNotContainKey("patient_output");
        assertThat(summary.metadataSummary()).doesNotContainKey("input");
    }

    @Test
    void reviewSummaryMapsDecisionFields() {
        CandidateReviewRecord record = new CandidateReviewRecord(
                "review_001",
                "exp_cand_001",
                CandidateKind.EXPERIENCE_CANDIDATE,
                CandidateReviewStatus.REVIEW_REQUIRED,
                CandidateReviewStatus.APPROVED,
                CandidateReviewDecision.APPROVE,
                "Looks valid after manual check.",
                "reviewer_a",
                Instant.parse("2026-06-25T12:00:00Z"),
                CandidateTestFixtures.sampleSourceRef(),
                Map.of());

        ReviewConsoleSummaryDto summary = mapper.toReviewSummary(record);

        assertThat(summary.decision()).isEqualTo("APPROVE");
        assertThat(summary.sourceRef().caseId()).isEqualTo("chest_pain_high_risk_001");
    }

    private static EvaluationRun sampleEvaluationRun() {
        EvaluationItemResult item = new EvaluationItemResult(
                "eval_run_001",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                true,
                0.95,
                null,
                List.of(),
                List.of(),
                List.of("Safety gate triggered as expected"));
        EvaluationResult result = new EvaluationResult(
                "eval_run_001",
                "phase3-default",
                "0.3.0",
                "phase2-default",
                "0.2.0",
                1,
                1,
                0,
                1.0,
                0.95,
                1.0,
                1.0,
                0.8,
                1.0,
                1.0,
                List.of());
        return new EvaluationRun(
                "eval_run_001",
                EvaluationTestFixtures.sampleRunConfig(),
                EvaluationRunStatus.COMPLETED,
                Instant.parse("2026-06-26T06:00:00Z"),
                Instant.parse("2026-06-26T06:05:00Z"),
                List.of(item),
                result);
    }

    private static AuditLogRecord sampleAuditRecord() {
        return new AuditLogRecord(
                "audit_001",
                "req_001",
                "tester",
                AuditActionType.CREATE_RUNTIME,
                AuditResourceType.RUNTIME,
                "rt_001",
                AuditResultStatus.SUCCESS,
                Instant.parse("2026-06-30T10:00:00Z"),
                Map.of("mode", "in-memory"));
    }
}
