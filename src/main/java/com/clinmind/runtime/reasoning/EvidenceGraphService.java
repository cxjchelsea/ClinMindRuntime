package com.clinmind.runtime.reasoning;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceRetrievalSnapshot;
import com.clinmind.runtime.evidence.mapper.EvidenceCandidateToGraphMapper;
import com.clinmind.runtime.asset.AssetRuntimeSupport;
import com.clinmind.runtime.asset.TestRecommendationAsset;
import com.clinmind.runtime.provider.TestRecommendationProvider;
import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.state.CaseFrame;
import com.clinmind.runtime.state.DDxCandidate;
import com.clinmind.runtime.state.EvidenceGraph;
import com.clinmind.runtime.state.EvidenceGraphItem;
import com.clinmind.runtime.state.EvidenceGraphRefEntry;
import com.clinmind.runtime.state.KnowledgeContext;
import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.SymptomItem;
import com.clinmind.runtime.state.UserInput;
import com.clinmind.runtime.trace.TraceStep;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Service;

@Service
public class EvidenceGraphService {

    private final TestRecommendationProvider testRecommendationProvider;

    public EvidenceGraphService(TestRecommendationProvider testRecommendationProvider) {
        this.testRecommendationProvider = testRecommendationProvider;
    }

    @TraceStep("EvidenceGraph")
    public EvidenceGraph buildEvidenceGraph(RuntimeState state) {
        List<EvidenceGraphItem> items = new ArrayList<>();
        String combinedText = combinedText(state);
        KnowledgeContext knowledge = state.getKnowledgeContext();
        CaseFrame caseFrame = state.getCaseFrame();
        List<EvidenceCandidate> ragCandidates = acceptedRagCandidates(state);
        List<String> ragSummaries = EvidenceCandidateToGraphMapper.toSupportingSummaries(ragCandidates);
        List<EvidenceGraphRefEntry> ragRefs = EvidenceCandidateToGraphMapper.toGraphRefs(ragCandidates);

        for (DDxCandidate candidate : state.getDifferentialBoard().candidates()) {
            List<String> supporting = new ArrayList<>(buildSupportingEvidence(caseFrame, candidate));
            supporting.addAll(ragSummaries);
            List<String> missing = buildMissingEvidence(caseFrame, knowledge, candidate, combinedText);
            List<String> nextQuestions = buildNextQuestions(knowledge, combinedText, ragCandidates);
            List<String> recommendedTests = buildRecommendedTests(state, knowledge, candidate);

            items.add(new EvidenceGraphItem(
                    candidate.name(),
                    supporting,
                    List.of(),
                    missing,
                    List.of(),
                    candidate.status(),
                    nextQuestions,
                    recommendedTests,
                    ragRefs));
        }
        return new EvidenceGraph(items);
    }

    private List<EvidenceCandidate> acceptedRagCandidates(RuntimeState state) {
        EvidenceRetrievalSnapshot snapshot = state.getEvidenceRetrieval();
        if (snapshot == null || snapshot.acceptedCandidates().isEmpty()) {
            return List.of();
        }
        return snapshot.acceptedCandidates();
    }

    private List<String> buildNextQuestions(
            KnowledgeContext knowledge, String combinedText, List<EvidenceCandidate> ragCandidates) {
        List<String> questions = buildNextQuestions(knowledge, combinedText);
        for (EvidenceCandidate candidate : ragCandidates) {
            if (candidate.useCase() != null
                    && candidate.useCase().name().equals("ASK_MORE")
                    && candidate.reasonSummary() != null) {
                questions.add(candidate.reasonSummary());
            }
        }
        return questions;
    }

    private List<String> buildSupportingEvidence(CaseFrame caseFrame, DDxCandidate candidate) {
        if (caseFrame == null || caseFrame.symptoms().isEmpty()) {
            return List.of();
        }
        List<String> supporting = new ArrayList<>();
        for (SymptomItem symptom : caseFrame.symptoms()) {
            StringBuilder detail = new StringBuilder(symptom.name());
            if (symptom.trigger() != null) {
                detail.append(" triggered by ").append(symptom.trigger());
            }
            if (symptom.severity() != null) {
                detail.append(" severity=").append(symptom.severity());
            }
            supporting.add(detail.toString());
        }
        if (caseFrame.chiefComplaint() != null) {
            supporting.add("chief complaint: " + caseFrame.chiefComplaint());
        }
        if (candidate.status() == CandidateStatus.POSSIBLE || candidate.status() == CandidateStatus.POSSIBLE_AFTER_EXCLUSION) {
            supporting.add("retained as " + candidate.status().getValue());
        }
        return supporting;
    }

    private List<String> buildMissingEvidence(
            CaseFrame caseFrame,
            KnowledgeContext knowledge,
            DDxCandidate candidate,
            String combinedText) {
        Set<String> missing = new LinkedHashSet<>();
        if (caseFrame != null) {
            for (String slot : caseFrame.missingSlots()) {
                missing.add("missing slot: " + slot);
            }
        }
        if (candidate.status() == CandidateStatus.NEED_TO_RULE_OUT
                || candidate.status() == CandidateStatus.MUST_NOT_MISS) {
            missing.add("high risk diagnosis not ruled out: " + candidate.name());
        }
        for (String question : knowledge.requiredQuestions()) {
            if (!isQuestionAnswered(question, combinedText)) {
                missing.add("unanswered question: " + question);
            }
        }
        return List.copyOf(missing);
    }

    private List<String> buildNextQuestions(KnowledgeContext knowledge, String combinedText) {
        List<String> questions = new ArrayList<>();
        for (String question : knowledge.requiredQuestions()) {
            if (!isQuestionAnswered(question, combinedText)) {
                questions.add(question);
            }
        }
        return questions;
    }

    private List<String> buildRecommendedTests(
            RuntimeState state,
            KnowledgeContext knowledge,
            DDxCandidate candidate) {
        if (candidate.status() != CandidateStatus.NEED_TO_RULE_OUT) {
            return List.of();
        }
        List<TestRecommendationAsset> rules = testRecommendationProvider.loadTestRecommendations(
                knowledge.symptomGroup(), AssetRuntimeSupport.queryContext(state));
        for (TestRecommendationAsset rule : rules) {
            AssetRuntimeSupport.recordAssetUsed(state, rule.metadata(), "EvidenceGraph");
            if (rule.targetStatus() == CandidateStatus.NEED_TO_RULE_OUT) {
                return rule.recommendedTests();
            }
        }
        return knowledge.recommendedTests();
    }

    private boolean isQuestionAnswered(String question, String combinedText) {
        if (question.contains("活动")) {
            return combinedText.contains("活动") || combinedText.contains("走路");
        }
        if (question.contains("出汗") || question.contains("呼吸")) {
            return combinedText.contains("出汗") || combinedText.contains("呼吸");
        }
        if (question.contains("体温") || question.contains("热")) {
            return combinedText.contains("体温") || combinedText.contains("热") || combinedText.contains("发烧");
        }
        return combinedText.contains(question);
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
