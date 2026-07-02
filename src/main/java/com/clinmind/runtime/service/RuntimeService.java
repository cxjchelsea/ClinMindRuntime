package com.clinmind.runtime.service;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.api.ContinueRuntimeRequest;
import com.clinmind.runtime.api.StartRuntimeRequest;
import com.clinmind.runtime.api.UserInputRequest;
import com.clinmind.runtime.boundary.DecisionBoundaryService;
import com.clinmind.runtime.boundary.FailurePolicyService;
import com.clinmind.runtime.caseframe.CaseFrameService;
import com.clinmind.runtime.entry.EntryAssessmentService;
import com.clinmind.runtime.experience.ExperienceContextService;
import com.clinmind.runtime.knowledge.KnowledgeContextService;
import com.clinmind.runtime.api.AssetContextRequest;
import com.clinmind.runtime.agent.AgentOrchestrationSnapshot;
import com.clinmind.runtime.agent.capability.CapabilityOrchestrationService;
import com.clinmind.runtime.evidence.EvidenceRetrievalSnapshot;
import com.clinmind.runtime.evidence.capability.EvidenceCapabilityOrchestrator;
import com.clinmind.runtime.agent.inquiry.InquiryQuestionCandidate;
import com.clinmind.runtime.asset.AssetLoadErrorCode;
import com.clinmind.runtime.asset.AssetLoadException;
import com.clinmind.runtime.asset.AssetPackageManifest;
import com.clinmind.runtime.asset.AssetPackageRepository;
import com.clinmind.runtime.output.ClinicianReportService;
import com.clinmind.runtime.output.PatientOutputService;
import com.clinmind.runtime.reasoning.DifferentialDiagnosisBoardService;
import com.clinmind.runtime.reasoning.EvidenceGraphService;
import com.clinmind.runtime.reasoning.QuestionTestPolicyService;
import com.clinmind.runtime.safety.SafetyGateService;
import com.clinmind.runtime.state.DecisionBoundaryResult;
import com.clinmind.runtime.state.EntryAssessmentResult;
import com.clinmind.runtime.state.ExperienceContext;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.PatientOutput;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.state.WorkMode;
import com.clinmind.runtime.storage.RuntimeStore;
import com.clinmind.runtime.trace.RuntimeTraceCollector;
import com.clinmind.runtime.trace.TraceContextHolder;
import com.clinmind.runtime.trace.TraceStepLog;
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
    private final RuntimeTraceCollector traceCollector;
    private final AssetPackageRepository assetPackageRepository;
    private final AuditLogService auditLogService;
    private final CapabilityOrchestrationService capabilityOrchestrationService;
    private final EvidenceCapabilityOrchestrator evidenceCapabilityOrchestrator;

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
            FailurePolicyService failurePolicyService,
            RuntimeTraceCollector traceCollector,
            AssetPackageRepository assetPackageRepository,
            AuditLogService auditLogService,
            CapabilityOrchestrationService capabilityOrchestrationService,
            EvidenceCapabilityOrchestrator evidenceCapabilityOrchestrator) {
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
        this.traceCollector = traceCollector;
        this.assetPackageRepository = assetPackageRepository;
        this.auditLogService = auditLogService;
        this.capabilityOrchestrationService = capabilityOrchestrationService;
        this.evidenceCapabilityOrchestrator = evidenceCapabilityOrchestrator;
    }

    public RuntimeExecutionResult startRuntime(StartRuntimeRequest request) {
        RuntimeState state = RuntimeState.createDefault(request.sessionId());
        state.setUserId(request.userId());
        state.setMode(request.mode());

        UserInput userInput = toUserInput(request.input());
        state.getInputHistory().add(userInput);

        TraceContextHolder.setRuntimeId(state.getRuntimeId());
        try {
            bindAssetPackage(state, request.assetContext());
            state.setRuntimeStatus(RuntimeStatus.ENTRY_ASSESSING);

            EntryAssessmentResult entry = entryAssessmentService.assessEntry(userInput, request.basicInfo());
            state.setEntryAssessment(entry);
            state.setWorkMode(entry.workMode());
            state.setRuntimeStatus(statusAfterEntry(entry.workMode()));

            if (entry.workMode() != WorkMode.UNSUPPORTED) {
                state.setCaseFrame(caseFrameService.buildOrUpdateCaseFrame(
                        userInput, state.getCaseFrame(), request.basicInfo()));
                if (isClinicalWorkMode(entry.workMode())) {
                    runClinicalPipeline(state);
                }
            }

            return persistStartResult(state, 1, userInput, request.basicInfo());
        } catch (AssetLoadException error) {
            failurePolicyService.handleFailure("AssetProvider", error, state);
            return persistStartResult(state, 1, userInput, request.basicInfo());
        } finally {
            traceCollector.drainStepsForRuntime(state.getRuntimeId());
            TraceContextHolder.clear();
        }
    }

    private RuntimeExecutionResult persistStartResult(
            RuntimeState state,
            int step,
            UserInput userInput,
            Map<String, Object> basicInfo) {
        RuntimeTrace trace = buildTrace(state, step, userInput, basicInfo);
        runtimeStore.create(state);
        runtimeStore.addTrace(trace);
        state.getRuntimeTraceIds().add(trace.getTraceId());
        state.bumpVersion();
        runtimeStore.update(state);
        auditLogService.record(
                AuditActionType.CREATE_RUNTIME,
                AuditResourceType.RUNTIME,
                state.getRuntimeId(),
                AuditResultStatus.SUCCESS,
                Map.of("runtime_status", state.getRuntimeStatus().getValue()));
        return new RuntimeExecutionResult(state, trace);
    }

    public RuntimeExecutionResult continueRuntime(ContinueRuntimeRequest request) {
        RuntimeState state = runtimeStore.get(request.runtimeId());
        UserInput userInput = toUserInput(request.input());
        state.getInputHistory().add(userInput);

        TraceContextHolder.setRuntimeId(state.getRuntimeId());
        try {
            state.setCaseFrame(caseFrameService.buildOrUpdateCaseFrame(
                    userInput, state.getCaseFrame(), null));

            if (isClinicalWorkMode(state.getWorkMode())) {
                runClinicalPipeline(state);
            } else if (state.getWorkMode() != WorkMode.UNSUPPORTED
                    && state.getRuntimeStatus() != RuntimeStatus.ERROR_SAFE_HALTED
                    && state.getRuntimeStatus() != RuntimeStatus.COMPLETED) {
                state.setRuntimeStatus(RuntimeStatus.WAITING_FOR_USER);
            }

            int step = runtimeStore.getTraces(request.runtimeId()).size() + 1;
            RuntimeTrace trace = buildTrace(state, step, userInput, null);
            runtimeStore.addTrace(trace);
            state.getRuntimeTraceIds().add(trace.getTraceId());
            state.bumpVersion();
            runtimeStore.update(state);
            auditLogService.record(
                    AuditActionType.CONTINUE_RUNTIME,
                    AuditResourceType.RUNTIME,
                    state.getRuntimeId(),
                    AuditResultStatus.SUCCESS,
                    Map.of("runtime_status", state.getRuntimeStatus().getValue(), "step", step));
            return new RuntimeExecutionResult(state, trace);
        } finally {
            traceCollector.drainStepsForRuntime(state.getRuntimeId());
            TraceContextHolder.clear();
        }
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

    public Map<String, Object> getAssetsUsed(String runtimeId) {
        RuntimeState state = runtimeStore.get(runtimeId);
        return com.clinmind.runtime.api.dto.AssetApiMapper.toAssetsUsedResponse(state);
    }

    private void runClinicalPipeline(RuntimeState state) {
        try {
            KnowledgeContext knowledgeContext = knowledgeContextService.buildKnowledgeContext(
                    state.getCaseFrame(), state.getEntryAssessment(), state);
            state.setKnowledgeContext(knowledgeContext);

            ExperienceContext experienceContext = experienceContextService.buildExperienceContext(
                    state.getCaseFrame(), knowledgeContext, state);
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

            AgentOrchestrationSnapshot orchestration = capabilityOrchestrationService.orchestrate(state);
            state.setAgentOrchestration(orchestration);

            EvidenceRetrievalSnapshot evidenceRetrieval = evidenceCapabilityOrchestrator.orchestrate(state);
            state.setEvidenceRetrieval(evidenceRetrieval);

            state.setDifferentialBoard(differentialDiagnosisBoardService.buildDifferentialBoard(state));
            state.setEvidenceGraph(evidenceGraphService.buildEvidenceGraph(state));
            state.setQuestionTestPolicy(questionTestPolicyService.decideNextAction(state));
            runOutputPipeline(state);
            if (state.getRuntimeStatus() != RuntimeStatus.ERROR_SAFE_HALTED) {
                state.setRuntimeStatus(resolveStatusAfterPolicy(state));
            }
        } catch (AssetLoadException error) {
            failurePolicyService.handleFailure("AssetProvider", error, state);
        }
    }

    private void bindAssetPackage(RuntimeState state, AssetContextRequest assetContext) {
        String packageId = assetContext != null && assetContext.packageId() != null
                && !assetContext.packageId().isBlank()
                ? assetContext.packageId()
                : assetPackageRepository.getDefaultPackageId();
        AssetPackageManifest manifest = assetPackageRepository.loadRuntimeManifest(packageId);
        String requestedVersion = assetContext != null ? assetContext.version() : null;
        if (requestedVersion != null && !requestedVersion.isBlank()) {
            if (!requestedVersion.equals(manifest.version())) {
                throw new AssetLoadException(
                        AssetLoadErrorCode.ASSET_VERSION_MISMATCH,
                        "Requested asset package version mismatch: requested="
                                + requestedVersion + ", actual=" + manifest.version(),
                        true,
                        packageId,
                        null);
            }
        }
        state.setAssetPackageId(packageId);
        state.setAssetPackageVersion(manifest.version());
    }

    private void runOutputPipeline(RuntimeState state) {
        DecisionBoundaryResult boundary = decisionBoundaryService.decideOutputBoundary(state);
        state.setDecisionBoundary(boundary);
        if (isFailSafeBoundary(boundary)) {
            failurePolicyService.handleFailure(
                    "DecisionBoundary",
                    new RuntimeException(boundary.reason()),
                    state);
            return;
        }
        state.setPatientOutput(patientOutputService.buildPatientOutput(state));
        state.setClinicianReport(clinicianReportService.buildClinicianReport(state));
    }

    private boolean isClinicalWorkMode(WorkMode workMode) {
        return workMode == WorkMode.CLINICAL_MODE || workMode == WorkMode.EMERGENCY_HINT;
    }

    private boolean isFailSafeBoundary(DecisionBoundaryResult boundary) {
        return boundary != null && boundary.constraints().contains("fail_safe");
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
        List<TraceStepLog> executedSteps = traceCollector.drainStepsForRuntime(state.getRuntimeId());
        for (TraceStepLog stepLog : executedSteps) {
            if (stepLog.success()) {
                trace.recordModule(stepLog.moduleName());
            }
        }

        Map<String, Object> outputSummary = new LinkedHashMap<>();
        if (state.getEntryAssessment() != null) {
            outputSummary.put("work_mode", state.getEntryAssessment().workMode().getValue());
            outputSummary.put("symptom_group", state.getEntryAssessment().symptomGroup());
        }
        if (trace.getModulesExecuted().contains("CaseFrameBuilder") && state.getCaseFrame() != null) {
            outputSummary.put("chief_complaint", state.getCaseFrame().chiefComplaint());
            outputSummary.put("missing_slots", state.getCaseFrame().missingSlots());
        }
        if (trace.getModulesExecuted().contains("KnowledgeContext")) {
            KnowledgeContext knowledge = state.getKnowledgeContext();
            if (knowledge != null) {
                knowledge.sourceAssets().forEach(trace::recordKnowledge);
                outputSummary.put("must_not_miss_count", knowledge.mustNotMiss().size());
            }
        }
        if (trace.getModulesExecuted().contains("SafetyGate") && state.getSafetyGate() != null) {
            SafetyGateResult safetyGate = state.getSafetyGate();
            trace.setSafetyGateResult(safetyGate);
            outputSummary.put("safety_gate_triggered", safetyGate.triggered());
        }
        if (trace.getModulesExecuted().contains("DifferentialDiagnosisBoard")
                && state.getDifferentialBoard() != null
                && !state.getDifferentialBoard().candidates().isEmpty()) {
            Map<String, Object> ddxChange = new LinkedHashMap<>();
            ddxChange.put("candidate_count", state.getDifferentialBoard().candidates().size());
            ddxChange.put("updated_reason", state.getDifferentialBoard().updatedReason());
            trace.setDdxChange(ddxChange);
        }
        if (trace.getModulesExecuted().contains("EvidenceGraph")
                && state.getEvidenceGraph() != null
                && !state.getEvidenceGraph().items().isEmpty()) {
            Map<String, Object> evidenceChange = new LinkedHashMap<>();
            evidenceChange.put("item_count", state.getEvidenceGraph().items().size());
            trace.setEvidenceGraphChange(evidenceChange);
        }
        if (trace.getModulesExecuted().contains("QuestionTestPolicy")) {
            QuestionTestPolicyResult policy = state.getQuestionTestPolicy();
            if (policy != null && policy.nextAction() != null) {
                outputSummary.put("next_action_type", policy.nextAction().type().getValue());
                outputSummary.put("next_action_content", policy.nextAction().content());
            }
        }
        if (trace.getModulesExecuted().contains("CapabilityOrchestration")
                && state.getAgentOrchestration() != null) {
            AgentOrchestrationSnapshot orchestration = state.getAgentOrchestration();
            outputSummary.put("agent_execution_id", orchestration.executionId());
            outputSummary.put("agent_status", orchestration.status() == null
                    ? null
                    : orchestration.status().name());
            outputSummary.put("agent_accepted_question_count", orchestration.acceptedQuestions().size());
        }
        if (trace.getModulesExecuted().contains("DecisionBoundary") && state.getDecisionBoundary() != null) {
            DecisionBoundaryResult boundary = state.getDecisionBoundary();
            trace.setDecisionBoundaryResult(boundary);
            outputSummary.put("allowed_output_level", boundary.allowedOutputLevel().getValue());
        }
        if (state.getPatientOutput() != null && state.getPatientOutput().allowed()) {
            outputSummary.put("patient_output_level", state.getPatientOutput().outputLevel().getValue());
        }
        if (basicInfo != null) {
            outputSummary.put("basic_info_applied", true);
        }
        if (state.getAssetPackageId() != null) {
            outputSummary.put("asset_package_id", state.getAssetPackageId());
            outputSummary.put("asset_package_version", state.getAssetPackageVersion());
        }
        if (trace.getModulesExecuted().contains("ExperienceContext") && state.getAssetsUsed() != null) {
            state.getAssetsUsed().stream()
                    .filter(record -> "ExperienceContext".equals(record.moduleName()))
                    .map(com.clinmind.runtime.asset.AssetUsedRecord::assetRef)
                    .forEach(trace::recordExperience);
        }
        outputSummary.put("runtime_status", state.getRuntimeStatus().getValue());
        outputSummary.put("trace_step_count", executedSteps.size());
        trace.setOutputSummary(outputSummary);
        return trace;
    }

    private UserInput toUserInput(UserInputRequest request) {
        return new UserInput(request.text(), request.attachments());
    }
}
