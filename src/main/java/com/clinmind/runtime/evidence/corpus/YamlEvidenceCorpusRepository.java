package com.clinmind.runtime.evidence.corpus;

import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidenceRiskLevel;
import com.clinmind.runtime.evidence.EvidenceUseCase;
import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class YamlEvidenceCorpusRepository implements EvidenceCorpusRepository {

    private static final Logger log = LoggerFactory.getLogger(YamlEvidenceCorpusRepository.class);
    private static final String CORPUS_PATH = "evidence/phase7-default/evidence_chunks.yml";

    private final Map<String, EvidenceCorpus> corpusByPackageId = new LinkedHashMap<>();
    private boolean available;

    @PostConstruct
    void loadCorpus() {
        try {
            EvidenceCorpus corpus = parseCorpus(loadYamlRoot());
            corpusByPackageId.put(corpus.packageId(), corpus);
            available = !corpus.chunks().isEmpty();
            log.info("Loaded evidence corpus {} v{} with {} chunks",
                    corpus.packageId(), corpus.version(), corpus.chunkCount());
        } catch (Exception ex) {
            available = false;
            log.warn("Failed to load evidence corpus: {}", ex.getMessage());
        }
    }

    @Override
    public EvidenceCorpus loadDefaultCorpus() {
        return corpusByPackageId.getOrDefault(
                EvidenceConstants.DEFAULT_CORPUS_PACKAGE_ID,
                new EvidenceCorpus(EvidenceConstants.DEFAULT_CORPUS_PACKAGE_ID, "", List.of()));
    }

    @Override
    public Optional<EvidenceCorpus> findByPackageId(String packageId) {
        return Optional.ofNullable(corpusByPackageId.get(packageId));
    }

    @Override
    public List<EvidenceChunk> findBySymptomGroup(String symptomGroup) {
        return loadDefaultCorpus().chunks().stream()
                .filter(chunk -> symptomGroup != null && symptomGroup.equals(chunk.symptomGroup()))
                .toList();
    }

    @Override
    public boolean isAvailable() {
        return available;
    }

    @SuppressWarnings("unchecked")
    private EvidenceCorpus parseCorpus(Map<String, Object> root) {
        String packageId = stringValue(root.get("package_id"), EvidenceConstants.DEFAULT_CORPUS_PACKAGE_ID);
        String version = stringValue(root.get("version"), EvidenceConstants.DEFAULT_CORPUS_VERSION);
        List<Map<String, Object>> rawChunks = (List<Map<String, Object>>) root.getOrDefault("chunks", List.of());
        List<EvidenceChunk> chunks = new ArrayList<>();
        for (Map<String, Object> raw : rawChunks) {
            EvidenceChunk chunk = parseChunk(raw);
            if (chunk != null) {
                chunks.add(chunk);
            }
        }
        return new EvidenceCorpus(packageId, version, chunks);
    }

    private EvidenceChunk parseChunk(Map<String, Object> raw) {
        String chunkId = stringValue(raw.get("chunk_id"), null);
        String sourceId = stringValue(raw.get("source_id"), null);
        String symptomGroup = stringValue(raw.get("symptom_group"), null);
        if (chunkId == null || sourceId == null || symptomGroup == null) {
            log.warn("Skipping evidence chunk with missing chunk_id/source_id/symptom_group");
            return null;
        }
        return new EvidenceChunk(
                chunkId,
                sourceId,
                stringValue(raw.get("source_type"), "synthetic_safety_knowledge"),
                stringValue(raw.get("title"), chunkId),
                stringValue(raw.get("section_path"), ""),
                symptomGroup,
                stringList(raw.get("diagnosis_tags")),
                stringList(raw.get("keyword_tags")),
                stringValue(raw.get("content_summary"), ""),
                stringValue(raw.get("evidence_strength"), "GUIDELINE_SUMMARY"),
                parseRiskLevel(stringValue(raw.get("risk_level"), "MEDIUM")),
                parseUseCases(stringList(raw.get("use_cases"))),
                stringValue(raw.get("version"), "0.1.0"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlRoot() throws Exception {
        ClassPathResource resource = new ClassPathResource(CORPUS_PATH);
        try (InputStream inputStream = resource.getInputStream()) {
            Object loaded = new Yaml().load(inputStream);
            if (loaded instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
            throw new IllegalStateException("evidence corpus root must be a map");
        }
    }

    private static String stringValue(Object value, String defaultValue) {
        if (value == null) {
            return defaultValue;
        }
        String text = String.valueOf(value).trim();
        return text.isEmpty() ? defaultValue : text;
    }

    @SuppressWarnings("unchecked")
    private static List<String> stringList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        for (Object item : list) {
            if (item != null) {
                result.add(String.valueOf(item));
            }
        }
        return List.copyOf(result);
    }

    private static EvidenceRiskLevel parseRiskLevel(String value) {
        if (value == null) {
            return EvidenceRiskLevel.MEDIUM;
        }
        try {
            return EvidenceRiskLevel.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException ex) {
            return EvidenceRiskLevel.MEDIUM;
        }
    }

    private static List<EvidenceUseCase> parseUseCases(List<String> values) {
        List<EvidenceUseCase> useCases = new ArrayList<>();
        for (String value : values) {
            EvidenceUseCase useCase = EvidenceUseCase.fromValue(value);
            if (useCase != null) {
                useCases.add(useCase);
            }
        }
        return List.copyOf(useCases);
    }
}
