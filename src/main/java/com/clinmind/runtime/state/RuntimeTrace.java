package com.clinmind.runtime.state;

import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class RuntimeTrace {

    private String traceId;
    private String runtimeId;
    private int step;
    private String input;
    private List<String> modulesExecuted;
    private List<String> knowledgeUsed;
    private List<String> experienceUsed;
    private SafetyGateResult safetyGateResult;
    private Map<String, Object> ddxChange;
    private Map<String, Object> evidenceGraphChange;
    private DecisionBoundaryResult decisionBoundaryResult;
    private Map<String, Object> outputSummary;
    private Instant createdAt;

    public RuntimeTrace() {
    }

    public static RuntimeTrace create(String runtimeId, int step, String input) {
        RuntimeTrace trace = new RuntimeTrace();
        trace.traceId = IdGenerator.traceId();
        trace.runtimeId = runtimeId;
        trace.step = step;
        trace.input = input == null ? "" : input;
        trace.modulesExecuted = new ArrayList<>();
        trace.knowledgeUsed = new ArrayList<>();
        trace.experienceUsed = new ArrayList<>();
        trace.createdAt = Instant.now();
        return trace;
    }

    public void recordModule(String moduleName) {
        if (moduleName != null && !modulesExecuted.contains(moduleName)) {
            modulesExecuted.add(moduleName);
        }
    }

    public void recordKnowledge(String asset) {
        if (asset != null && !knowledgeUsed.contains(asset)) {
            knowledgeUsed.add(asset);
        }
    }

    public void recordExperience(String unitId) {
        if (unitId != null && !experienceUsed.contains(unitId)) {
            experienceUsed.add(unitId);
        }
    }

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public void setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
    }

    public int getStep() {
        return step;
    }

    public void setStep(int step) {
        this.step = step;
    }

    public String getInput() {
        return input;
    }

    public void setInput(String input) {
        this.input = input;
    }

    public List<String> getModulesExecuted() {
        return modulesExecuted;
    }

    public void setModulesExecuted(List<String> modulesExecuted) {
        this.modulesExecuted = modulesExecuted;
    }

    public List<String> getKnowledgeUsed() {
        return knowledgeUsed;
    }

    public void setKnowledgeUsed(List<String> knowledgeUsed) {
        this.knowledgeUsed = knowledgeUsed;
    }

    public List<String> getExperienceUsed() {
        return experienceUsed;
    }

    public void setExperienceUsed(List<String> experienceUsed) {
        this.experienceUsed = experienceUsed;
    }

    public SafetyGateResult getSafetyGateResult() {
        return safetyGateResult;
    }

    public void setSafetyGateResult(SafetyGateResult safetyGateResult) {
        this.safetyGateResult = safetyGateResult;
    }

    public Map<String, Object> getDdxChange() {
        return ddxChange;
    }

    public void setDdxChange(Map<String, Object> ddxChange) {
        this.ddxChange = ddxChange;
    }

    public Map<String, Object> getEvidenceGraphChange() {
        return evidenceGraphChange;
    }

    public void setEvidenceGraphChange(Map<String, Object> evidenceGraphChange) {
        this.evidenceGraphChange = evidenceGraphChange;
    }

    public DecisionBoundaryResult getDecisionBoundaryResult() {
        return decisionBoundaryResult;
    }

    public void setDecisionBoundaryResult(DecisionBoundaryResult decisionBoundaryResult) {
        this.decisionBoundaryResult = decisionBoundaryResult;
    }

    public Map<String, Object> getOutputSummary() {
        return outputSummary;
    }

    public void setOutputSummary(Map<String, Object> outputSummary) {
        this.outputSummary = outputSummary == null ? null : new LinkedHashMap<>(outputSummary);
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }
}
