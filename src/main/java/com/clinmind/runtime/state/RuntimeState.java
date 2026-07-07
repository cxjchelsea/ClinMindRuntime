package com.clinmind.runtime.state;

import com.clinmind.runtime.agent.AgentOrchestrationSnapshot;
import com.clinmind.runtime.evidence.EvidenceRetrievalSnapshot;
import com.clinmind.runtime.evidence.graph.GraphEvidenceSnapshot;
import com.clinmind.runtime.modelgov.ModelGovernanceSnapshot;
import com.clinmind.runtime.provider.ProviderEnhancementSnapshot;
import com.clinmind.runtime.provider.ProviderGovernanceSnapshot;
import com.clinmind.runtime.asset.AssetUsedRecord;
import com.clinmind.runtime.toolgov.ToolGovernanceSnapshot;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public class RuntimeState {

    private String runtimeId;
    private String sessionId;
    private String userId;
    private int version;
    private RuntimeStatus runtimeStatus;
    private WorkMode workMode;
    private RuntimeMode mode;
    private List<UserInput> inputHistory;
    private EntryAssessmentResult entryAssessment;
    private CaseFrame caseFrame;
    private KnowledgeContext knowledgeContext;
    private ExperienceContext experienceContext;
    private SafetyGateResult safetyGate;
    private DifferentialDiagnosisBoard differentialBoard;
    private EvidenceGraph evidenceGraph;
    private QuestionTestPolicyResult questionTestPolicy;
    private DecisionBoundaryResult decisionBoundary;
    private PatientOutput patientOutput;
    private ClinicianReport clinicianReport;
    private List<String> runtimeTraceIds;
    private String assetPackageId;
    private String assetPackageVersion;
    private List<AssetUsedRecord> assetsUsed;
    private AgentOrchestrationSnapshot agentOrchestration;
    private EvidenceRetrievalSnapshot evidenceRetrieval;
    private GraphEvidenceSnapshot graphEvidence;
    private ProviderEnhancementSnapshot providerEnhancement;
    private ProviderGovernanceSnapshot providerGovernance;
    private ModelGovernanceSnapshot modelGovernance;
    private ToolGovernanceSnapshot toolGovernance;
    private Instant createdAt;
    private Instant updatedAt;

    public RuntimeState() {
    }

    public static RuntimeState createDefault(String sessionId) {
        Instant now = Instant.now();
        RuntimeState state = new RuntimeState();
        state.runtimeId = IdGenerator.runtimeId();
        state.sessionId = sessionId;
        state.version = 1;
        state.runtimeStatus = RuntimeStatus.CREATED;
        state.mode = RuntimeMode.PATIENT_FACING;
        state.inputHistory = new ArrayList<>();
        state.caseFrame = new CaseFrame();
        state.knowledgeContext = new KnowledgeContext();
        state.experienceContext = new ExperienceContext();
        state.differentialBoard = new DifferentialDiagnosisBoard();
        state.evidenceGraph = new EvidenceGraph();
        state.runtimeTraceIds = new ArrayList<>();
        state.assetsUsed = new ArrayList<>();
        state.assetPackageId = "phase2-default";
        state.createdAt = now;
        state.updatedAt = now;
        return state;
    }

    public void touch() {
        this.updatedAt = Instant.now();
    }

    public void bumpVersion() {
        this.version += 1;
        touch();
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public RuntimeStatus getRuntimeStatus() {
        return runtimeStatus;
    }

    public void setRuntimeStatus(RuntimeStatus runtimeStatus) {
        this.runtimeStatus = runtimeStatus;
    }

    public WorkMode getWorkMode() {
        return workMode;
    }

    public void setWorkMode(WorkMode workMode) {
        this.workMode = workMode;
    }

    public RuntimeMode getMode() {
        return mode;
    }

    public void setMode(RuntimeMode mode) {
        this.mode = mode;
    }

    public List<UserInput> getInputHistory() {
        return inputHistory;
    }

    public void setInputHistory(List<UserInput> inputHistory) {
        this.inputHistory = inputHistory;
    }

    public EntryAssessmentResult getEntryAssessment() {
        return entryAssessment;
    }

    public void setEntryAssessment(EntryAssessmentResult entryAssessment) {
        this.entryAssessment = entryAssessment;
    }

    public CaseFrame getCaseFrame() {
        return caseFrame;
    }

    public void setCaseFrame(CaseFrame caseFrame) {
        this.caseFrame = caseFrame;
    }

    public KnowledgeContext getKnowledgeContext() {
        return knowledgeContext;
    }

    public void setKnowledgeContext(KnowledgeContext knowledgeContext) {
        this.knowledgeContext = knowledgeContext;
    }

    public ExperienceContext getExperienceContext() {
        return experienceContext;
    }

    public void setExperienceContext(ExperienceContext experienceContext) {
        this.experienceContext = experienceContext;
    }

    public SafetyGateResult getSafetyGate() {
        return safetyGate;
    }

    public void setSafetyGate(SafetyGateResult safetyGate) {
        this.safetyGate = safetyGate;
    }

    public DifferentialDiagnosisBoard getDifferentialBoard() {
        return differentialBoard;
    }

    public void setDifferentialBoard(DifferentialDiagnosisBoard differentialBoard) {
        this.differentialBoard = differentialBoard;
    }

    public EvidenceGraph getEvidenceGraph() {
        return evidenceGraph;
    }

    public void setEvidenceGraph(EvidenceGraph evidenceGraph) {
        this.evidenceGraph = evidenceGraph;
    }

    public QuestionTestPolicyResult getQuestionTestPolicy() {
        return questionTestPolicy;
    }

    public void setQuestionTestPolicy(QuestionTestPolicyResult questionTestPolicy) {
        this.questionTestPolicy = questionTestPolicy;
    }

    public DecisionBoundaryResult getDecisionBoundary() {
        return decisionBoundary;
    }

    public void setDecisionBoundary(DecisionBoundaryResult decisionBoundary) {
        this.decisionBoundary = decisionBoundary;
    }

    public PatientOutput getPatientOutput() {
        return patientOutput;
    }

    public void setPatientOutput(PatientOutput patientOutput) {
        this.patientOutput = patientOutput;
    }

    public ClinicianReport getClinicianReport() {
        return clinicianReport;
    }

    public void setClinicianReport(ClinicianReport clinicianReport) {
        this.clinicianReport = clinicianReport;
    }

    public List<String> getRuntimeTraceIds() {
        return runtimeTraceIds;
    }

    public void setRuntimeTraceIds(List<String> runtimeTraceIds) {
        this.runtimeTraceIds = runtimeTraceIds;
    }

    public String getAssetPackageId() {
        return assetPackageId;
    }

    public void setAssetPackageId(String assetPackageId) {
        this.assetPackageId = assetPackageId;
    }

    public String getAssetPackageVersion() {
        return assetPackageVersion;
    }

    public void setAssetPackageVersion(String assetPackageVersion) {
        this.assetPackageVersion = assetPackageVersion;
    }

    public List<AssetUsedRecord> getAssetsUsed() {
        return assetsUsed;
    }

    public void setAssetsUsed(List<AssetUsedRecord> assetsUsed) {
        this.assetsUsed = assetsUsed;
    }

    public AgentOrchestrationSnapshot getAgentOrchestration() {
        return agentOrchestration;
    }

    public void setAgentOrchestration(AgentOrchestrationSnapshot agentOrchestration) {
        this.agentOrchestration = agentOrchestration;
    }

    public EvidenceRetrievalSnapshot getEvidenceRetrieval() {
        return evidenceRetrieval;
    }

    public void setEvidenceRetrieval(EvidenceRetrievalSnapshot evidenceRetrieval) {
        this.evidenceRetrieval = evidenceRetrieval;
    }

    public GraphEvidenceSnapshot getGraphEvidence() {
        return graphEvidence;
    }

    public void setGraphEvidence(GraphEvidenceSnapshot graphEvidence) {
        this.graphEvidence = graphEvidence;
    }

    public ProviderEnhancementSnapshot getProviderEnhancement() {
        return providerEnhancement;
    }

    public void setProviderEnhancement(ProviderEnhancementSnapshot providerEnhancement) {
        this.providerEnhancement = providerEnhancement;
    }

    public ProviderGovernanceSnapshot getProviderGovernance() {
        return providerGovernance;
    }

    public void setProviderGovernance(ProviderGovernanceSnapshot providerGovernance) {
        this.providerGovernance = providerGovernance;
    }

    public ModelGovernanceSnapshot getModelGovernance() {
        return modelGovernance;
    }

    public void setModelGovernance(ModelGovernanceSnapshot modelGovernance) {
        this.modelGovernance = modelGovernance;
    }

    public ToolGovernanceSnapshot getToolGovernance() {
        return toolGovernance;
    }

    public void setToolGovernance(ToolGovernanceSnapshot toolGovernance) {
        this.toolGovernance = toolGovernance;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
