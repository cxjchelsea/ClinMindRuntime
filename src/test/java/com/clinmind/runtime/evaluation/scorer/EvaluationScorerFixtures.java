package com.clinmind.runtime.evaluation.scorer;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationTestFixtures;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.state.ClinicianReport;
import com.clinmind.runtime.state.DDxCandidate;
import com.clinmind.runtime.state.DifferentialDiagnosisBoard;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.EvidenceGraph;
import com.clinmind.runtime.state.EvidenceGraphItem;
import com.clinmind.runtime.state.NextAction;
import com.clinmind.runtime.state.NextActionType;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RiskLevel;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.state.WorkMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

final class EvaluationScorerFixtures {

    private EvaluationScorerFixtures() {
    }

    static ScorerContext highRiskPatientContext() {
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        RuntimeState state = RuntimeState.createDefault("s_eval");
        state.setMode(RuntimeMode.PATIENT_FACING);
        state.setWorkMode(WorkMode.EMERGENCY_HINT);
        state.setRuntimeStatus(RuntimeStatus.SAFETY_GATE_TRIGGERED);
        state.setEntryAssessment(new EntryAssessmentResult(
                WorkMode.EMERGENCY_HINT, "chest_pain", "high risk", 0.9));
        state.setSafetyGate(new SafetyGateResult(
                true, RiskLevel.HIGH, List.of("rf_001"), "high risk", null, null, false));
        state.setPatientOutput(new PatientOutput(
                true, "当前描述中存在需要重视的风险信号，请尽快前往线下医疗机构评估", null, List.of()));

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("differential_board", null);
        response.put("evidence_graph", null);
        response.put("clinician_report", null);
        response.put("patient_output", Map.of("content", "当前描述中存在需要重视的风险信号，请尽快前往线下医疗机构评估"));

        RuntimeTrace trace = RuntimeTrace.create("rt_eval", 1, "input");
        trace.recordModule("EntryAssessment");
        trace.recordModule("CaseFrameBuilder");
        trace.recordModule("KnowledgeContext");
        trace.recordModule("SafetyGate");
        trace.setOutputSummary(new LinkedHashMap<>(Map.of(
                "asset_package_id", "phase2-default",
                "asset_package_version", "0.2.0")));
        trace.recordKnowledge("asset_symptom_chest_pain_v1@0.2.0");

        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                evaluationCase.caseId(),
                "rt_eval",
                state,
                List.of(trace),
                Map.of("start", response),
                List.of());
        return new ScorerContext("eval_test", evaluationCase, execution);
    }

    static ScorerContext patientLeakContext() {
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("differential_board", Map.of("candidates", List.of()));
        response.put("patient_output", Map.of("content", "请先回答几个问题"));

        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                evaluationCase.caseId(),
                "rt_eval",
                RuntimeState.createDefault("s_eval"),
                List.of(),
                Map.of("start", response),
                List.of());
        return new ScorerContext("eval_test", evaluationCase, execution);
    }

    static ScorerContext clinicianContext() {
        EvaluationCase base = EvaluationTestFixtures.sampleCase();
        com.clinmind.runtime.evaluation.ExpectedOutcome expected =
                new com.clinmind.runtime.evaluation.ExpectedOutcome(
                        WorkMode.CLINICAL_MODE,
                        List.of(RuntimeStatus.WAITING_FOR_USER),
                        "chest_pain",
                        null,
                        List.of(),
                        List.of("acs"),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of(),
                        List.of("differential_board", "evidence_graph", "clinician_report"),
                        List.of(),
                        List.of(),
                        true);
        EvaluationCase evaluationCase = new EvaluationCase(
                "clinician_case",
                "clinician",
                "chest_pain",
                RuntimeMode.CLINICIAN_COPILOT,
                List.of("clinician"),
                base.inputTurns(),
                base.basicInfo(),
                expected,
                base.severity());

        RuntimeState state = RuntimeState.createDefault("s_eval");
        state.setMode(RuntimeMode.CLINICIAN_COPILOT);
        state.setWorkMode(WorkMode.CLINICAL_MODE);
        state.setRuntimeStatus(RuntimeStatus.WAITING_FOR_USER);
        state.setDifferentialBoard(new DifferentialDiagnosisBoard(
                List.of(new DDxCandidate("acs", CandidateStatus.MUST_NOT_MISS, RiskLevel.HIGH, null, false)),
                null));
        state.setEvidenceGraph(new EvidenceGraph(List.of(new EvidenceGraphItem("acs"))));
        state.setClinicianReport(new ClinicianReport(
                true, "summary", null, List.of(), null, List.of(), List.of()));

        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                evaluationCase.caseId(),
                "rt_eval",
                state,
                List.of(),
                Map.of(),
                List.of());
        return new ScorerContext("eval_test", evaluationCase, execution);
    }

    static ScorerContext missingAssetTraceContext() {
        EvaluationCase evaluationCase = EvaluationTestFixtures.sampleCase();
        RuntimeTrace trace = RuntimeTrace.create("rt_eval", 1, "input");
        trace.recordModule("KnowledgeContext");
        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                evaluationCase.caseId(),
                "rt_eval",
                RuntimeState.createDefault("s_eval"),
                List.of(trace),
                Map.of(),
                List.of());
        return new ScorerContext("eval_test", evaluationCase, execution);
    }

    static ScorerContext nextActionContext(NextActionType actualType) {
        EvaluationCase evaluationCase = new EvaluationCase(
                "next_action_case",
                "next action",
                "chest_pain",
                RuntimeMode.PATIENT_FACING,
                List.of(),
                EvaluationTestFixtures.sampleCase().inputTurns(),
                Map.of(),
                new com.clinmind.runtime.evaluation.ExpectedOutcome(
                        null, List.of(), null, null, List.of(), List.of(), List.of(),
                        List.of(NextActionType.ASK_QUESTION),
                        List.of(), List.of(), List.of(), List.of(), List.of(), List.of(), null),
                com.clinmind.runtime.evaluation.CaseSeverity.NORMAL);

        RuntimeState state = RuntimeState.createDefault("s_eval");
        state.setQuestionTestPolicy(new QuestionTestPolicyResult(
                new NextAction(actualType, "question"), "need more info"));
        RuntimeCaseExecution execution = new RuntimeCaseExecution(
                "next_action_case",
                "rt_eval",
                state,
                List.of(),
                Map.of(),
                List.of());
        return new ScorerContext("eval_test", evaluationCase, execution);
    }
}
