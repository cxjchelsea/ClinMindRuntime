package com.clinmind.runtime.console.dto;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogRecord;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.candidate.CandidateTestFixtures;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationResult;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.UserInput;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ConsoleSensitiveFieldRedactionTest {

    private static final Set<String> DENYLIST = SensitiveFieldPolicy.METADATA_DENYLIST;
    private static final String PATIENT_TEXT = "胸口闷，活动后更明显，出汗";

    private SafeConsoleDtoMapper mapper;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        mapper = new SafeConsoleDtoMapper();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void serializedConsoleDtosDoNotContainSensitiveKeysOrPatientText() throws Exception {
        RuntimeState state = RuntimeState.createDefault("session_redaction");
        state.getInputHistory().add(new UserInput(PATIENT_TEXT));

        assertClean(mapper.toRuntimeSummary(state));
        assertClean(mapper.toRuntimeDetail(state));
        assertClean(mapper.toExperienceCandidateDetail(CandidateTestFixtures.sampleExperienceCandidate()));
        assertClean(mapper.toTrainingCandidateDetail(CandidateTestFixtures.sampleTrainingExampleCandidate()));

        EvaluationRun run = sampleEvaluationRun();
        assertClean(mapper.toEvaluationSummary(run));
        assertClean(mapper.toEvaluationDetail(run));

        var auditRecord = new AuditLogRecord(
                "audit_redaction",
                "req_001",
                "tester",
                AuditActionType.CREATE_RUNTIME,
                AuditResourceType.RUNTIME,
                "rt_001",
                AuditResultStatus.SUCCESS,
                Instant.parse("2026-06-30T10:00:00Z"),
                Map.of(
                        "mode", "jdbc",
                        "patient_output", PATIENT_TEXT,
                        "input_texts", List.of(PATIENT_TEXT),
                        "clinician_report", "internal report",
                        "input", Map.of("text", PATIENT_TEXT),
                        "notes", "free-form notes"));
        assertClean(mapper.toAuditSummary(auditRecord));
        assertClean(mapper.toAuditSummary(sampleAuditRecord()));
    }

    private static EvaluationRun sampleEvaluationRun() {
        EvaluationItemResult item = new EvaluationItemResult(
                "eval_run_redaction",
                "chest_pain_high_risk_001",
                "rt_sample001",
                List.of("trace_001"),
                true,
                0.95,
                null,
                List.of(),
                List.of(),
                List.of());
        EvaluationResult result = new EvaluationResult(
                "eval_run_redaction",
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
                "eval_run_redaction",
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

    private void assertClean(Object dto) throws Exception {
        String json = objectMapper.writeValueAsString(dto);
        assertThat(json).doesNotContain(PATIENT_TEXT);

        JsonNode root = objectMapper.readTree(json);
        assertNoDeniedKeys(root);
    }

    private void assertNoDeniedKeys(JsonNode node) {
        if (node.isObject()) {
            node.fieldNames().forEachRemaining(field -> {
                assertThat(DENYLIST).doesNotContain(field);
                assertNoDeniedKeys(node.get(field));
            });
        } else if (node.isArray()) {
            node.forEach(this::assertNoDeniedKeys);
        }
    }
}
