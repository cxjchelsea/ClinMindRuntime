package com.clinmind.runtime.evidence.phase12.retrieval.lexical;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceChunk;
import com.clinmind.runtime.evidence.phase12.EvidenceReviewStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceSourceType;
import com.clinmind.runtime.evidence.phase12.EvidenceSpan;
import com.clinmind.runtime.evidence.phase12.LicenseStatus;
import com.clinmind.runtime.evidence.phase12.SourceRegistryEntry;
import com.clinmind.runtime.evidence.phase12.SourceTrustStatus;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class InMemoryLexicalEvidenceRetriever implements LexicalEvidenceRetriever {

    private final ClinicalQuestionLexicalNormalizer normalizer;
    private final Map<String, SourceRegistryEntry> sourcesById;
    private final Map<String, EvidenceAssetVersion> assetsByVersionId;
    private final List<EvidenceChunk> chunks;
    private final Map<String, List<EvidenceSpan>> spansByChunkId;

    public InMemoryLexicalEvidenceRetriever(
            ClinicalQuestionLexicalNormalizer normalizer,
            List<SourceRegistryEntry> sources,
            List<EvidenceAssetVersion> assets,
            List<EvidenceChunk> chunks,
            List<EvidenceSpan> spans
    ) {
        this.normalizer = normalizer == null ? new ClinicalQuestionLexicalNormalizer() : normalizer;
        this.sourcesById = nullSafe(sources).stream().collect(Collectors.toUnmodifiableMap(SourceRegistryEntry::sourceId, Function.identity()));
        this.assetsByVersionId = nullSafe(assets).stream().collect(Collectors.toUnmodifiableMap(EvidenceAssetVersion::versionId, Function.identity()));
        this.chunks = List.copyOf(nullSafe(chunks));
        this.spansByChunkId = nullSafe(spans).stream().collect(Collectors.groupingBy(EvidenceSpan::chunkId));
    }

    @Override
    public List<LexicalRetrievalCandidate> retrieve(LexicalRetrievalRequest request) {
        Set<String> terms = tokenize(normalizer.normalize(request.queryText()));
        if (terms.isEmpty()) {
            return List.of();
        }
        List<ScoredChunk> scored = new ArrayList<>();
        for (EvidenceChunk chunk : chunks) {
            EvidenceAssetVersion asset = assetsByVersionId.get(chunk.versionId());
            if (asset == null) {
                continue;
            }
            SourceRegistryEntry source = sourcesById.get(asset.sourceId());
            if (source == null || !eligible(source, asset, request.scope())) {
                continue;
            }
            double score = score(terms, normalizer.normalize(chunk.normalizedText()));
            if (score > 0.0d) {
                scored.add(new ScoredChunk(chunk, source, asset, score));
            }
        }
        scored.sort(Comparator.comparingDouble(ScoredChunk::score).reversed()
                .thenComparing(scoredChunk -> scoredChunk.chunk().chunkId()));
        List<LexicalRetrievalCandidate> candidates = new ArrayList<>();
        for (int i = 0; i < scored.size() && candidates.size() < request.topK(); i++) {
            ScoredChunk scoredChunk = scored.get(i);
            EvidenceSpan span = firstSpan(scoredChunk.chunk()).orElse(null);
            if (span == null) {
                continue;
            }
            candidates.add(candidate(scoredChunk, span, candidates.size() + 1));
        }
        return List.copyOf(candidates);
    }

    private boolean eligible(SourceRegistryEntry source, EvidenceAssetVersion asset, EligibleEvidenceScope scope) {
        if (source.licenseStatus() != LicenseStatus.VERIFIED
                || source.trustStatus() == SourceTrustStatus.BLOCKED
                || source.reviewStatus() != EvidenceReviewStatus.APPROVED) {
            return false;
        }
        if (scope.isProduction()) {
            if (!asset.eligibleForProductionRetrieval(scope.asOf())) {
                return false;
            }
        } else if (asset.lifecycleStatus() == AssetLifecycleStatus.REVOKED || asset.lifecycleStatus() == AssetLifecycleStatus.DEPRECATED) {
            return false;
        }
        return matches(scope.sourceTypeFilters(), source.sourceType().name())
                && matches(scope.specialtyFilters(), asset.specialty())
                && matches(scope.jurisdictionFilters(), asset.jurisdiction())
                && matches(scope.languageFilters(), asset.language())
                && matches(scope.intendedAudienceFilters(), asset.intendedAudience());
    }

    private boolean matches(Set<String> filters, String value) {
        if (filters == null || filters.isEmpty()) {
            return true;
        }
        if (value == null || value.isBlank()) {
            return false;
        }
        return filters.stream().anyMatch(filter -> filter.equalsIgnoreCase(value));
    }

    private Optional<EvidenceSpan> firstSpan(EvidenceChunk chunk) {
        return spansByChunkId.getOrDefault(chunk.chunkId(), List.of()).stream()
                .min(Comparator.comparingInt(EvidenceSpan::startOffset).thenComparing(EvidenceSpan::spanId));
    }

    private LexicalRetrievalCandidate candidate(ScoredChunk scoredChunk, EvidenceSpan span, int rank) {
        EvidenceChunk chunk = scoredChunk.chunk();
        EvidenceAssetVersion asset = scoredChunk.asset();
        SourceRegistryEntry source = scoredChunk.source();
        return new LexicalRetrievalCandidate(
                "lex_" + chunk.chunkId(),
                source.sourceId(),
                asset.assetId(),
                asset.versionId(),
                chunk.chunkId(),
                span.spanId(),
                chunk.sectionPath(),
                span.locator(),
                span.quotedText(),
                chunk.textChecksum(),
                span.spanChecksum(),
                asset.checksum(),
                source.sourceType() == null ? EvidenceSourceType.OTHER : source.sourceType(),
                source.authorityLevel(),
                asset.specialty(),
                asset.jurisdiction(),
                asset.language(),
                asset.intendedAudience(),
                asset.publicationDate(),
                asset.effectiveFrom(),
                asset.effectiveTo(),
                scoredChunk.score(),
                rank);
    }

    private Set<String> tokenize(String text) {
        if (text == null || text.isBlank()) {
            return Set.of();
        }
        return java.util.Arrays.stream(text.split("[^\\p{IsAlphabetic}\\p{IsDigit}\\p{IsHan}]+"))
                .map(token -> token.toLowerCase(Locale.ROOT).trim())
                .filter(token -> token.length() >= 2)
                .collect(Collectors.toUnmodifiableSet());
    }

    private double score(Set<String> terms, String text) {
        if (text == null || text.isBlank()) {
            return 0.0d;
        }
        String lowered = text.toLowerCase(Locale.ROOT);
        double score = 0.0d;
        for (String term : terms) {
            if (lowered.contains(term)) {
                score += 1.0d;
            }
        }
        return score / terms.size();
    }

    private static <T> List<T> nullSafe(List<T> values) {
        return values == null ? List.of() : values;
    }

    private record ScoredChunk(EvidenceChunk chunk, SourceRegistryEntry source, EvidenceAssetVersion asset, double score) {}
}
