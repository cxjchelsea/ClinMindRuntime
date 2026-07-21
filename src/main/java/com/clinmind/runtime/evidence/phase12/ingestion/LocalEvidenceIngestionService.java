package com.clinmind.runtime.evidence.phase12.ingestion;

import com.clinmind.runtime.evidence.phase12.AssetLifecycleStatus;
import com.clinmind.runtime.evidence.phase12.EvidenceAssetVersion;
import com.clinmind.runtime.evidence.phase12.EvidenceChunk;
import com.clinmind.runtime.evidence.phase12.EvidenceSpan;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceAssetVersionRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceChunkRepository;
import com.clinmind.runtime.evidence.phase12.repository.EvidenceSpanRepository;
import com.clinmind.runtime.state.IdGenerator;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.List;
import java.util.Map;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnProperty(name = "clinmind.persistence.mode", havingValue = "postgres")
public class LocalEvidenceIngestionService implements EvidenceIngestionService {

    private static final int MAX_CONTENT_BYTES = 128 * 1024;
    private static final String CLASSPATH_PREFIX = "classpath:";
    private static final String ALLOWED_CLASSPATH_PREFIX = "evidence/phase12-p0/";

    private final EvidenceAssetVersionRepository assetVersionRepository;
    private final EvidenceChunkRepository chunkRepository;
    private final EvidenceSpanRepository spanRepository;
    private final MinimalMarkdownEvidenceParser parser = new MinimalMarkdownEvidenceParser();

    public LocalEvidenceIngestionService(
            EvidenceAssetVersionRepository assetVersionRepository,
            EvidenceChunkRepository chunkRepository,
            EvidenceSpanRepository spanRepository) {
        this.assetVersionRepository = assetVersionRepository;
        this.chunkRepository = chunkRepository;
        this.spanRepository = spanRepository;
    }

    @Override
    public EvidenceIngestionResult ingest(EvidenceIngestionCommand command) {
        Instant startedAt = Instant.now();
        String ingestionId = "ing_" + IdGenerator.evidenceRetrievalId().replace("evidence_ret_", "");
        String traceRef = IdGenerator.evidenceTraceId();
        try {
            EvidenceAssetVersion asset = assetVersionRepository.findByVersionId(command.versionId())
                    .orElseThrow(() -> new IllegalArgumentException("asset version not found: " + command.versionId()));
            String contentReference = selectContentReference(command, asset);
            validateContentReference(contentReference);
            LoadedContent loaded = loadContent(contentReference);
            String expectedChecksum = selectExpectedChecksum(command, asset);
            if (!loaded.checksum().equals(expectedChecksum)) {
                assetVersionRepository.save(withLifecycle(asset, AssetLifecycleStatus.QUARANTINED, loaded.bytes().length, null));
                return result(ingestionId, command, EvidenceIngestionStatus.QUARANTINED, loaded.checksum(), 0, 0,
                        List.of("checksum_mismatch"), "CHECKSUM_MISMATCH", traceRef, startedAt);
            }

            ParsedEvidenceDocument document = parser.parse(loaded.text());
            int spanCount = 0;
            for (ParsedChunk parsed : document.chunks()) {
                String chunkId = deterministicId("chunk", asset.versionId(), parsed.ordinal());
                EvidenceChunk chunk = new EvidenceChunk(
                        chunkId,
                        asset.versionId(),
                        parsed.sectionPath(),
                        parsed.ordinal(),
                        parsed.text(),
                        checksum(parsed.text().getBytes(StandardCharsets.UTF_8)),
                        tokenCount(parsed.text()),
                        Map.of(
                                "parser_version", MinimalMarkdownEvidenceParser.PARSER_VERSION,
                                "content_reference", contentReference));
                chunkRepository.save(chunk);

                EvidenceSpan span = new EvidenceSpan(
                        deterministicId("span", asset.versionId(), parsed.ordinal()),
                        chunkId,
                        asset.versionId(),
                        0,
                        parsed.text().length(),
                        parsed.text(),
                        checksum(parsed.text().getBytes(StandardCharsets.UTF_8)),
                        parsed.locator(),
                        parsed.spanType());
                spanRepository.save(span);
                spanCount++;
            }
            assetVersionRepository.save(withLifecycle(asset, AssetLifecycleStatus.INGESTED, loaded.bytes().length, Instant.now()));
            return result(ingestionId, command, EvidenceIngestionStatus.COMPLETED, loaded.checksum(), document.chunks().size(), spanCount,
                    List.of(), null, traceRef, startedAt);
        } catch (RuntimeException ex) {
            return result(ingestionId, command, EvidenceIngestionStatus.FAILED, null, 0, 0,
                    List.of(safeMessage(ex)), "EVIDENCE_INGESTION_FAILED", traceRef, startedAt);
        }
    }

    private EvidenceIngestionResult result(
            String ingestionId,
            EvidenceIngestionCommand command,
            EvidenceIngestionStatus status,
            String checksum,
            int chunkCount,
            int spanCount,
            List<String> warnings,
            String errorCode,
            String traceRef,
            Instant startedAt) {
        return new EvidenceIngestionResult(
                ingestionId,
                command.requestId(),
                command.versionId(),
                status,
                checksum,
                MinimalMarkdownEvidenceParser.PARSER_VERSION,
                chunkCount,
                spanCount,
                warnings,
                errorCode,
                traceRef,
                startedAt,
                Instant.now());
    }

    private String selectContentReference(EvidenceIngestionCommand command, EvidenceAssetVersion asset) {
        return command.contentReference() == null || command.contentReference().isBlank()
                ? asset.externalReference()
                : command.contentReference();
    }

    private String selectExpectedChecksum(EvidenceIngestionCommand command, EvidenceAssetVersion asset) {
        return command.expectedChecksum() == null || command.expectedChecksum().isBlank()
                ? asset.checksum()
                : command.expectedChecksum();
    }

    private void validateContentReference(String contentReference) {
        if (contentReference == null || contentReference.isBlank()) {
            throw new IllegalArgumentException("content_reference must not be blank");
        }
        if (!contentReference.startsWith(CLASSPATH_PREFIX)) {
            throw new IllegalArgumentException("only classpath content_reference is allowed in P12P0-D");
        }
        String path = contentReference.substring(CLASSPATH_PREFIX.length());
        if (!path.startsWith(ALLOWED_CLASSPATH_PREFIX) || path.contains("..") || path.contains("\\")) {
            throw new IllegalArgumentException("content_reference outside phase12-p0 allowlist");
        }
        String lower = path.toLowerCase(java.util.Locale.ROOT);
        if (!(lower.endsWith(".md") || lower.endsWith(".txt"))) {
            throw new IllegalArgumentException("unsupported content_reference extension");
        }
    }

    private LoadedContent loadContent(String contentReference) {
        String path = contentReference.substring(CLASSPATH_PREFIX.length());
        ClassPathResource resource = new ClassPathResource(path);
        if (!resource.exists()) {
            throw new IllegalArgumentException("content_reference does not exist");
        }
        try (InputStream inputStream = resource.getInputStream()) {
            byte[] bytes = inputStream.readAllBytes();
            if (bytes.length == 0 || bytes.length > MAX_CONTENT_BYTES) {
                throw new IllegalArgumentException("content size outside allowed range");
            }
            return new LoadedContent(bytes, checksum(bytes), new String(bytes, StandardCharsets.UTF_8));
        } catch (IOException ex) {
            throw new IllegalStateException("failed to read content_reference", ex);
        }
    }

    private EvidenceAssetVersion withLifecycle(
            EvidenceAssetVersion asset,
            AssetLifecycleStatus lifecycleStatus,
            long contentLength,
            Instant ingestedAt) {
        return new EvidenceAssetVersion(
                asset.assetId(),
                asset.versionId(),
                asset.sourceId(),
                asset.title(),
                asset.documentType(),
                asset.externalReference(),
                asset.specialty(),
                asset.intendedAudience(),
                asset.jurisdiction(),
                asset.language(),
                asset.publicationDate(),
                asset.effectiveFrom(),
                asset.effectiveTo(),
                asset.supersedesVersionId(),
                lifecycleStatus,
                asset.reviewStatus(),
                asset.checksum(),
                asset.mimeType(),
                contentLength,
                MinimalMarkdownEvidenceParser.PARSER_VERSION,
                asset.schemaVersion(),
                ingestedAt);
    }

    private String deterministicId(String prefix, String versionId, int ordinal) {
        return prefix + "_" + versionId + "_" + ordinal;
    }

    private int tokenCount(String text) {
        return text == null || text.isBlank() ? 0 : text.strip().split("\\s+").length;
    }

    private String checksum(byte[] bytes) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return "sha256:" + HexFormat.of().formatHex(digest.digest(bytes));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 not available", ex);
        }
    }

    private String safeMessage(RuntimeException ex) {
        String message = ex.getMessage();
        if (message == null || message.isBlank()) {
            return ex.getClass().getSimpleName();
        }
        return message.length() > 160 ? message.substring(0, 160) : message;
    }

    private record LoadedContent(byte[] bytes, String checksum, String text) {
    }
}