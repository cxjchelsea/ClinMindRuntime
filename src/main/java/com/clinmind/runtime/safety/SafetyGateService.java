package com.clinmind.runtime.safety;

import com.clinmind.runtime.asset.AssetRuntimeSupport;
import com.clinmind.runtime.asset.CapabilityProfileAsset;
import com.clinmind.runtime.provider.CapabilityProfileProvider;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RedFlagRule;
import com.clinmind.runtime.state.RiskLevel;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SafetyGateResult;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class SafetyGateService {

    private final CapabilityProfileProvider capabilityProfileProvider;

    public SafetyGateService(CapabilityProfileProvider capabilityProfileProvider) {
        this.capabilityProfileProvider = capabilityProfileProvider;
    }

    @TraceStep("SafetyGate")
    public SafetyGateResult evaluateSafety(RuntimeState state) {
        try {
            return evaluateInternal(state);
        } catch (Exception error) {
            return new SafetyGateResult(
                    true,
                    RiskLevel.UNKNOWN,
                    List.of(),
                    "safety gate failed",
                    "safe_halt",
                    "conservative_output_only",
                    true);
        }
    }

    private SafetyGateResult evaluateInternal(RuntimeState state) {
        KnowledgeContext knowledge = state.getKnowledgeContext();
        if (knowledge == null || knowledge.redFlags().isEmpty()) {
            return new SafetyGateResult();
        }

        String combinedText = combinedText(state);
        List<String> matchedRules = new ArrayList<>();
        RedFlagRule highestRule = null;

        for (RedFlagRule rule : knowledge.redFlags()) {
            if (matchesAllFeatures(rule, combinedText, state.getCaseFrame())) {
                matchedRules.add(rule.ruleId());
                if (highestRule == null || rule.riskLevel().ordinal() > highestRule.riskLevel().ordinal()) {
                    highestRule = rule;
                }
            }
        }

        if (matchedRules.isEmpty()) {
            return new SafetyGateResult();
        }

        CapabilityProfileAsset profile = capabilityProfileProvider.loadCapabilityProfile(
                knowledge.symptomGroup(), AssetRuntimeSupport.queryContext(state));
        AssetRuntimeSupport.recordAssetUsed(state, profile.metadata(), "SafetyGate");

        String requiredAction = highestRule.action();
        if (requiredAction == null) {
            requiredAction = "urgent_evaluation";
        }

        return new SafetyGateResult(
                true,
                highestRule.riskLevel(),
                matchedRules,
                "matched red flag rules: " + String.join(", ", matchedRules),
                requiredAction,
                highestRule.patientConstraint(),
                false);
    }

    private boolean matchesAllFeatures(RedFlagRule rule, String text, CaseFrame caseFrame) {
        for (String feature : rule.features()) {
            if (!hasFeature(feature, text, caseFrame)) {
                return false;
            }
        }
        return !rule.features().isEmpty();
    }

    private boolean hasFeature(String feature, String text, CaseFrame caseFrame) {
        return switch (feature) {
            case "activity_related" -> text.contains("活动后") || text.contains("走路")
                    || hasSymptomTrigger(caseFrame, "活动后");
            case "sweating" -> text.contains("出汗") || hasSymptomName(caseFrame, "sweating");
            case "severe_pain" -> text.contains("剧烈") || text.contains("严重");
            case "high_fever" -> text.contains("高烧") || text.contains("高热");
            case "altered_consciousness" -> text.contains("意识") || text.contains("昏迷");
            default -> text.contains(feature);
        };
    }

    private boolean hasSymptomName(CaseFrame caseFrame, String name) {
        if (caseFrame == null) {
            return false;
        }
        return caseFrame.symptoms().stream().anyMatch(item -> name.equals(item.name()));
    }

    private boolean hasSymptomTrigger(CaseFrame caseFrame, String trigger) {
        if (caseFrame == null) {
            return false;
        }
        return caseFrame.symptoms().stream()
                .anyMatch(item -> trigger.equals(item.trigger()) || "活动后".equals(item.trigger()));
    }

    private String combinedText(RuntimeState state) {
        StringBuilder builder = new StringBuilder();
        if (state.getCaseFrame() != null && state.getCaseFrame().chiefComplaint() != null) {
            builder.append(state.getCaseFrame().chiefComplaint()).append(' ');
        }
        for (UserInput input : state.getInputHistory()) {
            builder.append(input.text()).append(' ');
        }
        return builder.toString();
    }
}
