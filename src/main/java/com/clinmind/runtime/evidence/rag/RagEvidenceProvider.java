package com.clinmind.runtime.evidence.rag;

import com.clinmind.runtime.evidence.EvidenceCandidate;
import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidenceRef;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRetrievalResult;
import com.clinmind.runtime.evidence.EvidenceRetrievalStatus;
import com.clinmind.runtime.evidence.EvidenceRetrievalTrace;
import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import com.clinmind.runtime.evidence.EvidenceUseCase;
import com.clinmind.runtime.evidence.corpus.EvidenceChunk;
import com.clinmind.runtime.evidence.corpus.EvidenceCorpus;
import com.clinmind.runtime.evidence.corpus.EvidenceCorpusRepository;
import com.clinmind.runtime.state.IdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class RagEvidenceProvider implements EvidenceProvider {

    private final EvidenceCorpusRepository corpusRepository;

    public RagEvidenceProvider(EvidenceCorpusRepository corpusRepository) {
        this.corpusRepository = corpusRepository;
    }

    @Override
    public String providerId() {
        return EvidenceConstants.RAG_EVIDENCE_PROVIDER_ID;
    }

    @Override
    public String providerVersion() {
        return EvidenceConstants.RAG_EVIDENCE_PROVIDER_VERSION;
    }

    @Override
    public EvidenceRetrievalResult retrieve(EvidenceRetrievalRequest request) {
        Instant startedAt = Instant.now();
        String retrievalId = IdGenerator.evidenceRetrievalId();
        EvidenceCorpus corpus = corpusRepository.loadDefaultCorpus();

        int limit = normalizeLimit(request.retrievalLimit());
        List<String> queryTerms = buildQueryTerms(request);
        List<EvidenceChunk> chunks = corpusRepository.findBySymptomGroup(request.symptomGroup());

        List<ScoredChunk> scored = new ArrayList<>();
        for (EvidenceChunk chunk : chunks) {
            double score = scoreChunk(chunk, request, queryTerms);
            if (score >= EvidenceConstants.MIN_RETRIEVAL_SCORE) {
                scored.add(new ScoredChunk(chunk, score));
            }
        }
        scored.sort(Comparator.comparingDouble(ScoredChunk::score).reversed());

        List<EvidenceCandidate> candidates = new ArrayList<>();
        for (ScoredChunk item : scored.stream().limit(limit).toList()) {
            candidates.add(toCandidate(item, request, corpus));
        }

        EvidenceRetrievalStatus status = candidates.isEmpty()
                ? EvidenceRetrievalStatus.NO_EVIDENCE_FOUND
                : EvidenceRetrievalStatus.SUCCESS;

        EvidenceRetrievalTrace trace = new EvidenceRetrievalTrace(
                IdGenerator.evidenceTraceId(),
                retrievalId,
                request.runtimeId(),
                providerId(),
                providerVersion(),
                corpus.version(),
                buildInputSummary(request),
                Map.of("candidate_count", candidates.size()),
                null,
                null,
                queryTerms,
                scored.size(),
                List.of(),
                List.of(),
                List.of(),
                Instant.now());

        return new EvidenceRetrievalResult(
                retrievalId,
                request.requestId(),
                request.runtimeId(),
                providerId(),
                providerVersion(),
                corpus.version(),
                status,
                candidates,
                null,
                trace,
                List.of(),
                null,
                startedAt,
                Instant.now(),
                null);
    }

    private EvidenceCandidate toCandidate(ScoredChunk scored, EvidenceRetrievalRequest request, EvidenceCorpus corpus) {
        EvidenceChunk chunk = scored.chunk();
        EvidenceUseCase useCase = resolveUseCase(chunk, request);
        List<String> matchedFields = matchFields(chunk, request);
        String relatedDdx = request.candidateDdxSummary().isEmpty()
                ? chunk.diagnosisTags().isEmpty() ? null : chunk.diagnosisTags().get(0)
                : request.candidateDdxSummary().get(0);

        EvidenceRef ref = new EvidenceRef(
                "ev_" + chunk.chunkId(),
                chunk.sourceId(),
                chunk.chunkId(),
                chunk.sourceType(),
                chunk.title(),
                chunk.sectionPath(),
                chunk.symptomGroup(),
                chunk.diagnosisTags(),
                chunk.evidenceStrength(),
                useCase == EvidenceUseCase.SAFETY_WARNING ? "ASK_MORE" : "SUPPORT",
                chunk.riskLevel(),
                request.assetPackageId(),
                request.assetPackageVersion(),
                corpus.version(),
                providerId(),
                roundScore(scored.score()));

        return new EvidenceCandidate(
                IdGenerator.evidenceCandidateId(),
                ref,
                matchedFields,
                relatedDdx,
                useCase,
                roundScore(scored.score() * 0.95),
                buildReasonSummary(chunk, useCase));
    }

    private double scoreChunk(EvidenceChunk chunk, EvidenceRetrievalRequest request, List<String> queryTerms) {
        double score = 0.45;
        if (request.symptomGroup().equals(chunk.symptomGroup())) {
            score += 0.25;
        }
        String combined = String.join(" ", queryTerms).toLowerCase(Locale.ROOT);
        for (String keyword : chunk.keywordTags()) {
            if (combined.contains(keyword.toLowerCase(Locale.ROOT))) {
                score += 0.08;
            }
        }
        if (chunk.contentSummary() != null) {
            for (String term : queryTerms) {
                if (chunk.contentSummary().toLowerCase(Locale.ROOT).contains(term.toLowerCase(Locale.ROOT))) {
                    score += 0.04;
                }
            }
        }
        if (chunk.riskLevel() == EvidenceRiskLevel.HIGH && !request.redFlagSummary().isEmpty()) {
            score += 0.12;
        }
        for (String redFlag : request.redFlagSummary()) {
            for (String keyword : chunk.keywordTags()) {
                if (redFlag.toLowerCase(Locale.ROOT).contains(keyword.toLowerCase(Locale.ROOT))
                        || keyword.toLowerCase(Locale.ROOT).contains(redFlag.toLowerCase(Locale.ROOT))) {
                    score += 0.06;
                }
            }
        }
        return Math.min(score, 0.99);
    }

    private EvidenceUseCase resolveUseCase(EvidenceChunk chunk, EvidenceRetrievalRequest request) {
        if (chunk.riskLevel() == EvidenceRiskLevel.HIGH && !request.redFlagSummary().isEmpty()) {
            if (chunk.useCases().contains(EvidenceUseCase.SAFETY_WARNING)) {
                return EvidenceUseCase.SAFETY_WARNING;
            }
        }
        if (!request.missingFacts().isEmpty() && chunk.useCases().contains(EvidenceUseCase.ASK_MORE)) {
            return EvidenceUseCase.ASK_MORE;
        }
        if (chunk.useCases().contains(EvidenceUseCase.SUPPORT)) {
            return EvidenceUseCase.SUPPORT;
        }
        return chunk.useCases().isEmpty() ? EvidenceUseCase.ASK_MORE : chunk.useCases().get(0);
    }

    private List<String> matchFields(EvidenceChunk chunk, EvidenceRetrievalRequest request) {
        Set<String> matched = new LinkedHashSet<>();
        String combinedKnown = String.join(" ", request.knownFacts()).toLowerCase(Locale.ROOT);
        for (String keyword : chunk.keywordTags()) {
            if (combinedKnown.contains(keyword.toLowerCase(Locale.ROOT))) {
                matched.add(keyword);
            }
        }
        request.redFlagSummary().stream()
                .filter(flag -> chunk.contentSummary().contains(flag) || combinedKnown.contains(flag.toLowerCase(Locale.ROOT)))
                .forEach(matched::add);
        return List.copyOf(matched);
    }

    private List<String> buildQueryTerms(EvidenceRetrievalRequest request) {
        Set<String> terms = new LinkedHashSet<>();
        terms.add(request.symptomGroup());
        terms.addAll(request.knownFacts());
        terms.addAll(request.redFlagSummary());
        Object chiefComplaint = request.caseFrameSummary().get("chief_complaint");
        if (chiefComplaint != null) {
            terms.add(String.valueOf(chiefComplaint));
        }
        return List.copyOf(terms);
    }

    private Map<String, Object> buildInputSummary(EvidenceRetrievalRequest request) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("runtime_id", request.runtimeId());
        summary.put("symptom_group", request.symptomGroup());
        summary.put("known_fact_count", request.knownFacts().size());
        summary.put("red_flag_count", request.redFlagSummary().size());
        return summary;
    }

    private String buildReasonSummary(EvidenceChunk chunk, EvidenceUseCase useCase) {
        return switch (useCase) {
            case SAFETY_WARNING -> "当前输入匹配高风险证据片段：" + chunk.title() + "，建议医生端关注并补充追问。";
            case ASK_MORE -> "证据提示需补充问诊信息：" + chunk.title() + "。";
            default -> "证据支持当前鉴别方向：" + chunk.title() + "。";
        };
    }

    private int normalizeLimit(int limit) {
        if (limit <= 0) {
            return EvidenceConstants.DEFAULT_RETRIEVAL_LIMIT;
        }
        return Math.min(limit, EvidenceConstants.MAX_RETRIEVAL_LIMIT);
    }

    private double roundScore(double score) {
        return Math.round(score * 100.0) / 100.0;
    }

    private record ScoredChunk(EvidenceChunk chunk, double score) {
    }
}
