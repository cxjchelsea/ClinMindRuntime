package com.clinmind.runtime.boundary;

import com.clinmind.runtime.asset.AssetRuntimeSupport;
import com.clinmind.runtime.asset.CapabilityProfileAsset;
import com.clinmind.runtime.knowledge.CapabilityProfile;
import com.clinmind.runtime.provider.CapabilityProfileProvider;
import com.clinmind.runtime.state.DecisionBoundaryResult;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.NextAction;
import com.clinmind.runtime.state.NextActionType;
import com.clinmind.runtime.state.OutputLevel;
import com.clinmind.runtime.state.QuestionTestPolicyResult;
import com.clinmind.runtime.state.RuntimeMode;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class DecisionBoundaryService {

    private final CapabilityProfileProvider capabilityProfileProvider;

    public DecisionBoundaryService(CapabilityProfileProvider capabilityProfileProvider) {
        this.capabilityProfileProvider = capabilityProfileProvider;
    }

    @TraceStep("DecisionBoundary")
    public DecisionBoundaryResult decideOutputBoundary(RuntimeState state) {
        try {
            return decideInternal(state);
        } catch (Exception error) {
            return failSafeBoundary("decision boundary failed: " + error.getMessage());
        }
    }

    private DecisionBoundaryResult decideInternal(RuntimeState state) {
        KnowledgeContext knowledge = state.getKnowledgeContext();
        CapabilityProfile profile = null;
        if (knowledge != null && knowledge.symptomGroup() != null) {
            CapabilityProfileAsset profileAsset = capabilityProfileProvider.loadCapabilityProfile(
                    knowledge.symptomGroup(), AssetRuntimeSupport.queryContext(state));
            AssetRuntimeSupport.recordAssetUsed(state, profileAsset.metadata(), "DecisionBoundary");
            profile = AssetRuntimeSupport.toCapabilityProfile(profileAsset);
        }

        SafetyGateResult safetyGate = state.getSafetyGate();
        QuestionTestPolicyResult policy = state.getQuestionTestPolicy();
        boolean highRiskActive = safetyGate != null && safetyGate.triggered();
        boolean clinicianMode = isClinicianMode(state.getMode());

        List<String> constraints = new ArrayList<>();
        constraints.add("no_definitive_diagnosis");
        constraints.add("no_prescription");

        if (highRiskActive) {
            constraints.add("no_low_risk_reassurance");
            if (safetyGate.patientOutputConstraint() != null) {
                constraints.add(safetyGate.patientOutputConstraint());
            }
        }

        OutputLevel allowedLevel = resolveAllowedOutputLevel(profile, policy, highRiskActive);
        if (!isPatientOutputAllowed(profile, allowedLevel)) {
            allowedLevel = OutputLevel.O1_CONTINUE_QUESTIONING;
            constraints.add("capability_profile_restricted");
        }

        boolean clinicianDdxAllowed = clinicianMode && profileAllowsClinicianDdx(profile);

        String reason = highRiskActive
                ? "high risk active; patient output tightened"
                : "default patient-safe output boundary";

        return new DecisionBoundaryResult(
                allowedLevel,
                false,
                clinicianDdxAllowed,
                reason,
                List.copyOf(constraints));
    }

    private OutputLevel resolveAllowedOutputLevel(
            CapabilityProfile profile,
            QuestionTestPolicyResult policy,
            boolean highRiskActive) {
        if (highRiskActive) {
            return OutputLevel.O5_VISIT_OR_URGENT_CARE_RECOMMENDATION;
        }
        if (policy == null || policy.nextAction() == null) {
            return OutputLevel.O1_CONTINUE_QUESTIONING;
        }
        NextAction action = policy.nextAction();
        return switch (action.type()) {
            case RECOMMEND_VISIT -> OutputLevel.O5_VISIT_OR_URGENT_CARE_RECOMMENDATION;
            case RECOMMEND_TEST -> OutputLevel.O2_RISK_HINT;
            case ASK_QUESTION, WAIT_FOR_USER -> OutputLevel.O1_CONTINUE_QUESTIONING;
            default -> OutputLevel.O1_CONTINUE_QUESTIONING;
        };
    }

    private boolean isPatientOutputAllowed(CapabilityProfile profile, OutputLevel level) {
        if (profile == null || profile.patientAllowedOutputs() == null) {
            return true;
        }
        return profile.patientAllowedOutputs().contains(level.getValue());
    }

    private boolean profileAllowsClinicianDdx(CapabilityProfile profile) {
        if (profile == null || profile.clinicianAllowedOutputs() == null) {
            return true;
        }
        return profile.clinicianAllowedOutputs().stream()
                .anyMatch(output -> output.startsWith("O3_") || output.startsWith("O7_"));
    }

    private boolean isClinicianMode(RuntimeMode mode) {
        return mode == RuntimeMode.CLINICIAN_COPILOT || mode == RuntimeMode.DEBUG;
    }

    public DecisionBoundaryResult failSafeBoundary(String reason) {
        return new DecisionBoundaryResult(
                OutputLevel.O1_CONTINUE_QUESTIONING,
                false,
                false,
                reason,
                List.of("fail_safe", "no_definitive_diagnosis", "no_prescription", "no_low_risk_reassurance"));
    }
}
