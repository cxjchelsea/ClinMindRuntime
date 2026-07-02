package com.clinmind.runtime.agent.inquiry;

import com.clinmind.runtime.agent.AgentConstants;
import com.clinmind.runtime.agent.AgentMetadata;
import com.clinmind.runtime.agent.registry.AgentRegistry;
import com.clinmind.runtime.state.IdGenerator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class InquiryPlanningAgent {

    private final AgentRegistry agentRegistry;

    public InquiryPlanningAgent(AgentRegistry agentRegistry) {
        this.agentRegistry = agentRegistry;
    }

    public InquiryPlanProposal plan(InquiryPlanningInput input) {
        AgentMetadata metadata = agentRegistry
                .findById(AgentConstants.INQUIRY_PLANNING_AGENT_ID)
                .orElseThrow(() -> new IllegalStateException("InquiryPlanningAgent not registered"));

        List<ScoredFact> scoredFacts = scoreMissingFacts(input);
        int maxCount = Math.min(input.maxQuestionCount(), AgentConstants.ABSOLUTE_MAX_QUESTION_COUNT);

        List<InquiryQuestionCandidate> questions = new ArrayList<>();
        Set<String> usedFacts = new LinkedHashSet<>();
        int index = 1;
        for (ScoredFact scoredFact : scoredFacts) {
            if (questions.size() >= maxCount) {
                break;
            }
            if (usedFacts.contains(scoredFact.fact())) {
                continue;
            }
            QuestionTemplate template = resolveTemplate(scoredFact.fact());
            if (template == null) {
                continue;
            }
            questions.add(new InquiryQuestionCandidate(
                    "q" + index++,
                    template.questionText(),
                    template.clinicalPurpose(),
                    scoredFact.fact(),
                    scoredFact.priority(),
                    scoredFact.riskRelated(),
                    true,
                    template.expectedAnswerType(),
                    true,
                    false));
            usedFacts.add(scoredFact.fact());
        }

        String reasoning = buildReasoningSummary(input, questions);
        return new InquiryPlanProposal(
                IdGenerator.agentProposalId(),
                input.runtimeId(),
                metadata.agentId(),
                metadata.agentVersion(),
                questions,
                reasoning,
                questions.isEmpty() ? "HIGH" : "MEDIUM",
                List.of(),
                List.of("不向患者暗示具体诊断。"),
                null);
    }

    private List<ScoredFact> scoreMissingFacts(InquiryPlanningInput input) {
        List<ScoredFact> scored = new ArrayList<>();
        for (String fact : input.missingFacts()) {
            boolean riskRelated = isRedFlagRelated(fact, input.redFlagCandidates());
            InquiryQuestionPriority priority = riskRelated ? InquiryQuestionPriority.HIGH : defaultPriority(fact);
            scored.add(new ScoredFact(fact, priority, riskRelated));
        }
        scored.sort(Comparator
                .comparingInt((ScoredFact item) -> priorityRank(item.priority()))
                .reversed()
                .thenComparing(ScoredFact::fact));
        return scored;
    }

    private int priorityRank(InquiryQuestionPriority priority) {
        return switch (priority) {
            case HIGH -> 3;
            case MEDIUM -> 2;
            case LOW -> 1;
        };
    }

    private InquiryQuestionPriority defaultPriority(String fact) {
        String normalized = fact.toLowerCase(Locale.ROOT);
        if (normalized.contains("放射") || normalized.contains("呼吸") || normalized.contains("红旗")) {
            return InquiryQuestionPriority.HIGH;
        }
        if (normalized.contains("持续") || normalized.contains("duration")) {
            return InquiryQuestionPriority.HIGH;
        }
        return InquiryQuestionPriority.MEDIUM;
    }

    private boolean isRedFlagRelated(String fact, List<String> redFlagCandidates) {
        for (String redFlag : redFlagCandidates) {
            if (fact.contains(redFlag) || redFlag.contains(fact)) {
                return true;
            }
        }
        String normalized = fact.toLowerCase(Locale.ROOT);
        return normalized.contains("放射") || normalized.contains("呼吸") || normalized.contains("红旗");
    }

    private QuestionTemplate resolveTemplate(String fact) {
        String normalized = fact.toLowerCase(Locale.ROOT);
        if (normalized.contains("持续") || normalized.contains("duration") || normalized.contains("多久")) {
            return new QuestionTemplate(
                    "这种不适大概持续了多久？是几分钟、半小时以上，还是更久？",
                    "clarify_duration",
                    "duration");
        }
        if (normalized.contains("严重") || normalized.contains("severity")) {
            return new QuestionTemplate(
                    "目前这种不适的严重程度如何？是轻微可以忍受，还是已经明显影响日常活动？",
                    "clarify_severity",
                    "severity");
        }
        if (normalized.contains("放射") || normalized.contains("radiation")) {
            return new QuestionTemplate(
                    "这种不适有没有向其他部位放射或扩散？如果有，大概是哪些部位？",
                    "clarify_radiation",
                    "associated_symptom");
        }
        if (normalized.contains("呼吸") || normalized.contains("dyspnea") || normalized.contains("气短")) {
            return new QuestionTemplate(
                    "有没有出现呼吸困难、喘不上气，或者活动后明显加重的情况？",
                    "clarify_dyspnea",
                    "associated_symptom");
        }
        if (normalized.contains("病史") || normalized.contains("history") || normalized.contains("既往")) {
            return new QuestionTemplate(
                    "以前有没有类似情况，或者相关慢性病史、长期用药情况？",
                    "clarify_history",
                    "history");
        }
        return new QuestionTemplate(
                "关于「" + fact + "」，能否再补充一些具体情况？",
                "clarify_missing_fact",
                "free_text");
    }

    private String buildReasoningSummary(InquiryPlanningInput input, List<InquiryQuestionCandidate> questions) {
        if (questions.isEmpty()) {
            return "当前缺少可规划的追问信息。";
        }
        if (!input.redFlagCandidates().isEmpty()) {
            return "当前存在潜在高风险信号，需要优先补充持续时间、放射痛和呼吸困难等关键信息。";
        }
        return "当前仍有缺失信息，需要按优先级补充关键病史与症状细节。";
    }

    private record ScoredFact(String fact, InquiryQuestionPriority priority, boolean riskRelated) {
    }

    private record QuestionTemplate(String questionText, String clinicalPurpose, String expectedAnswerType) {
    }
}
