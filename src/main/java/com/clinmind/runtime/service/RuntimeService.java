package com.clinmind.runtime.service;

import com.clinmind.runtime.api.ContinueRuntimeRequest;
import com.clinmind.runtime.api.StartRuntimeRequest;
import com.clinmind.runtime.api.UserInputRequest;
import com.clinmind.runtime.boundary.DecisionBoundaryService;
import com.clinmind.runtime.boundary.FailurePolicyService;
import com.clinmind.runtime.caseframe.CaseFrameService;
import com.clinmind.runtime.entry.EntryAssessmentService;
import com.clinmind.runtime.experience.ExperienceContextService;
import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.output.ClinicianReportService;
import com.clinmind.runtime.output.PatientOutputService;
import com.clinmind.runtime.reasoning.DifferentialDiagnosisBoardService;
import com.clinmind.runtime.reasoning.EvidenceGraphService;
import com.clinmind.runtime.reasoning.QuestionTestPolicyService;
import com.clinmind.runtime.safety.SafetyGateService;
import com.clinmind.runtime.state.DDxCandidate;
import com.clinmind.runtime.state.DecisionBoundaryResult;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.EvidenceGraph;
import com.clinmind.runtime.state.ExperienceContext;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.storage.RuntimeStore;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class RuntimeService {

    private final RuntimeStore runtimeStore;
    private final EntryAssessmentService entryAssessmentService;
    private final CaseFrameService caseFrameService;
    private final KnowledgeContextService knowledgeContextService;
    private final ExperienceContextService experienceContextService;
    private final SafetyGateService safetyGateService;
    private final DifferentialDiagnosisBoardService differentialDiagnosisBoardService;
    private final EvidenceGraphService evidenceGraphService;
    private final QuestionTestPolicyService questionTestPolicyService;
    private final DecisionBoundaryService decisionBoundaryService;
    private final PatientOutputService patientOutputService;
    private final ClinicianReportService clinicianReportService;
    private final FailurePolicyService failurePolicyService;

    public RuntimeService(
            RuntimeStore runtimeStore,
            EntryAssessmentService entryAssessmentService,
            CaseFrameService caseFrameService,
            KnowledgeContextService knowledgeContextService,
            ExperienceContextService experienceContextService,
            SafetyGateService safetyGateService,
            DifferentialDiagnosisBoardService differentialDiagnosisBoardService,
            EvidenceGraphService evidenceGraphService,
            QuestionTestPolicyService questionTestPolicyService,
            DecisionBoundaryService decisionBoundaryService,
            PatientOutputService patientOutputService,
            ClinicianReportService clinicianReportService,
            FailurePolicyService failurePolicyService) {
        this.runtimeStore = runtimeStore;
        this.entryAssessmentService = entryAssessmentService;
        this.caseFrameService = caseFrameService;
        this.knowledgeContextService = knowledgeContextService;
        this.experienceContextService = experienceContextService;
        this.safetyGateService = safetyGateService;
        this.differentialDiagnosisBoardService = differentialDiagnosisBoardService;
        this.evidenceGraphService = evidenceGraphService;
        this.questionTestPolicyService = questionTestPolicyService;
        this.decisionBoundaryService = decisionBoundaryService;
        this.patientOutputService = patientOutputService;
        this.clinicianReportService = clinicianReportService;
        this.failurePolicyService = failurePolicyService;
    }

    public RuntimeExecutionResult startRuntime(StartRuntimeRequest request) {
        RuntimeState state = RuntimeState.createDefault(request.sessionId());
        state.setUserId(request.userId());
        state.setMode(request.mode());
        state.setRuntimeStatus(RuntimeStatus.ENTRY_ASSESSING);

        UserInput userInput = toUserInput(request.input());
        state.getInputHistory().add(userInput);

        EntryAssessmentResult entry = entryAssessmentService.assessEntry(userInput, request.basicInfo());
        state.setEntryAssessment(entry);
        state.setWorkMode(entry.workMode());
        state.setRuntimeStatus(statusAfterEntry(entry.workMode()));

        if (entry.workMode() != WorkMode.UNSUPPORTED) {
            state.setCaseFrame(caseFrameService.buildOrUpdateCaseFrame(
                    userInput, state.getCaseFrame(), request.basicInfo()));
            runClinicalPipeline(state);
        }

        RuntimeTrace trace = buildTrace(state, 1, userInput, request.basicInfo());
        runtimeStore.create(state);
        runtimeStore.addTrace(trace);
        state.getRuntimeTraceIds().add(trace.getTraceId());
        state.bumpVersion();
        runtimeStore.update(state);
        return new RuntimeExecutionResult(state, trace);
    }

    public RuntimeExecutionResult continueRuntime(ContinueRuntimeRequest request) {
        RuntimeState state = runtimeStore.get(request.runtimeId());
        UserInput userInput = toUserInput(request.input());
        state.getInputHistory().add(userInput);

        state.setCaseFrame(caseFrameService.buildOrUpdateCaseFrame(
                userInput, state.getCaseFrame(), null));

        if (state.getWorkMode() != WorkMode.UNSUPPORTED && state.getWorkMode() != WorkMode.WELLNESS_MODE) {
            runClinicalPipeline(state);
        } else if (state.getRuntimeStatus() != RuntimeStatus.ERROR_SAFE_HALTED
                && state.getRuntimeStatus() != RuntimeStatus.COMPLETED) {
            state.setRuntimeStatus(RuntimeStatus.WAITING_FOR_USER);
        }

        int step = runtimeStore.getTraces(request.runtimeId()).size() + 1;
        RuntimeTrace trace = buildTrace(state, step, userInput, null);
        runtimeStore.addTrace(trace);
        state.getRuntimeTraceIds().add(trace.getTraceId());
        state.bumpVersion();
        runtimeStore.update(state);
        return new RuntimeExecutionResult(state, trace);
    }

    public RuntimeState getStatus(String runtimeId) {
        return runtimeStore.get(runtimeId);
    }

    public RuntimeState getResult(String runtimeId) {
        return runtimeStore.get(runtimeId);
    }

    public List<RuntimeTrace> getTraces(String runtimeId) {
        return runtimeStore.getTraces(runtimeId);
    }

    private void runClinicalPipeline(RuntimeState state) {
        KnowledgeContext knowledgeContext = knowledgeContextService.buildKnowledgeContext(
                state.getCaseFrame(), state.getEntryAssessment());
        state.setKnowledgeContext(knowledgeContext);

        ExperienceContext experienceContext = experienceContextService.buildExperienceContext(
                state.getCaseFrame(), knowledgeContext);
        state.setExperienceContext(experienceContext);

        SafetyGateResult safetyGate = safetyGateService.evaluateSafety(state);
        state.setSafetyGate(safetyGate);

        if (safetyGate.failSafeRequired()) {
            failurePolicyService.handleFailure(
                    "SafetyGate",
                    new RuntimeException("safety gate failed"),
                    state);
            return;
        }

        state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
        state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));
        state.setQuestionTestPolicy(questionTestPolicyService.decideNextAction(state));
        runOutputPipeline(state);
        state.setRuntimeStatus(resolveStatusAfterPolicy(state));
    }

    private void runOutputPipeline(RuntimeState state) {
        try {
            state.setDecisionBoundary(decisionBoundaryService.decideOutputBoundary(state));
            state.setPatientOutput(patientOutputService.buildPatientOutput(state));
            state.setClinicianReport(clinicianReportService.buildClinicianReport(state));
        } catch (Exception error) {
            failurePolicyService.handleFailure("DecisionBoundary", error, state);
        }
    }

    private RuntimeStatus resolveStatusAfterPolicy(RuntimeState state) {
        SafetyGateResult safetyGate = state.getSafetyGate();
        if (safetyGate != null && safetyGate.triggered()) {
            return RuntimeStatus.SAFETY_GATE_TRIGGERED;
        }
        QuestionTestPolicyResult policy = state.getQuestionTestPolicy();
        if (policy == null || policy.nextAction() == null) {
            return RuntimeStatus.COLLECTING_EVIDENCE;
        }
        return switch (policy.nextAction().type()) {
            case RECOMMEND_TEST -> RuntimeStatus.RECOMMENDING_TESTS;
            case ASK_QUESTION, WAIT_FOR_USER -> RuntimeStatus.WAITING_FOR_USER;
            case RECOMMEND_VISIT -> RuntimeStatus.SAFETY_GATE_TRIGGERED;
            default -> RuntimeStatus.COLLECTING_EVIDENCE;
        };
    }

    private RuntimeStatus statusAfterEntry(WorkMode workMode) {
        return switch (workMode) {
            case UNSUPPORTED -> RuntimeStatus.ERROR_SAFE_HALTED;
            case WELLNESS_MODE -> RuntimeStatus.WELLNESS_MODE;
            default -> RuntimeStatus.COLLECTING_CASE_INFO;
        };
    }

    private RuntimeTrace buildTrace(
            RuntimeState state,
            int step,
            UserInput userInput,
            Map<String, Object> basicInfo) {
        RuntimeTrace trace = RuntimeTrace.create(state.getRuntimeId(), step, userInput.text());
        trace.recordModule("EntryAssessment");

        Map<String, Object> outputSummary = new LinkedHashMap<>();
        if (state.getEntryAssessment() != null) {
            outputSummary.put("work_mode", state.getEntryAssessment().workMode().getValue());
            outputSummary.put("symptom_group", state.getEntryAssessment().symptomGroup());
        }
        if (state.getWorkMode() != WorkMode.UNSUPPORTED && state.getWorkMode() != WorkMode.WELLNESS_MODE) {
            trace.recordModule("CaseFrameBuilder");
            outputSummary.put("chief_complaint", state.getCaseFrame().chiefComplaint());
            outputSummary.put("missing_slots", state.getCaseFrame().missingSlots());

            trace.recordModule("KnowledgeContext");
            KnowledgeContext knowledge = state.getKnowledgeContext();
            if (knowledge != null) {
                knowledge.sourceAssets().forEach(trace::recordKnowledge);
                outputSummary.put("must_not_miss_count", knowledge.mustNotMiss().size());
            }

            trace.recordModule("ExperienceContext");
            trace.recordModule("SafetyGate");
            SafetyGateResult safetyGate = state.getSafetyGate();
            if (safetyGate != null) {
                trace.setSafetyGateResult(safetyGate);
                outputSummary.put("safety_gate_triggered", safetyGate.triggered());
            }

            trace.recordModule("DifferentialDiagnosisBoard");
            if (state.getDifferentialBoard() != null) {
                Map<String, Object> ddxChange = new LinkedHashMap<>();
                ddxChange.put("candidate_count", state.getDifferentialBoard().candidates().size());
                ddxChange.put("updated_reason", state.getDifferentialBoard().updatedReason());
                ddxChange.put("statuses", state.getDifferentialBoard().candidates().stream()
                        .map(DDxCandidate::status)
                        .map(Enum::name)
                        .toList());
                trace.setDdxChange(ddxChange);
            }

            trace.recordModule("EvidenceGraph");
            EvidenceGraph evidenceGraph = state.getEvidenceGraph();
            if (evidenceGraph != null && !evidenceGraph.items().isEmpty()) {
                Map<String, Object> evidenceChange = new LinkedHashMap<>();
                evidenceChange.put("item_count", evidenceGraph.items().size());
                evidenceChange.put("missing_evidence_count", evidenceGraph.items().stream()
                        .mapToInt(item -> item.missingEvidence().size())
                        .sum());
                trace.setEvidenceGraphChange(evidenceChange);
            }

            trace.recordModule("QuestionTestPolicy");
            QuestionTestPolicyResult policy = state.getQuestionTestPolicy();
            if (policy != null && policy.nextAction() != null) {
                outputSummary.put("next_action_type", policy.nextAction().type().getValue());
                outputSummary.put("next_action_content", policy.nextAction().content());
            }

            trace.recordModule("DecisionBoundary");
            DecisionBoundaryResult boundary = state.getDecisionBoundary();
            if (boundary != null) {
                trace.setDecisionBoundaryResult(boundary);
                outputSummary.put("allowed_output_level", boundary.allowedOutputLevel().getValue());
            }

            trace.recordModule("PatientOutput");
            PatientOutput patientOutput = state.getPatientOutput();
            if (patientOutput != null && patientOutput.allowed()) {
                outputSummary.put("patient_output_level", patientOutput.outputLevel().getValue());
            }

            if (state.getClinicianReport() != null && state.getClinicianReport().allowed()) {
                trace.recordModule("ClinicianReport");
                outputSummary.put("clinician_report_allowed", true);
            }
        }
        if (basicInfo != null) {
            outputSummary.put("basic_info_applied", true);
        }
        outputSummary.put("runtime_status", state.getRuntimeStatus().getValue());
        trace.setOutputSummary(outputSummary);
        return trace;
    }

    private UserInput toUserInput(UserInputRequest request) {
        return new UserInput(request.text(), request.attachments());
    }
}
