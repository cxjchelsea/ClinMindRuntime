package com.clinmind.runtime.evaluation.yaml;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationCaseRepository;
import com.clinmind.runtime.evaluation.EvaluationCaseSet;
import com.clinmind.runtime.evaluation.EvaluationLoadException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.stereotype.Component;
import org.yaml.snakeyaml.Yaml;

@Component
public class YamlEvaluationCaseRepository implements EvaluationCaseRepository {

    public static final String DEFAULT_CASE_SET_ID = "phase3-default";
    private static final String MANIFEST_FILE = "manifest.yml";

    private final String caseSetsPrefix;
    private final Yaml yaml = new Yaml();
    private final ObjectMapper objectMapper;
    private final ResourcePatternResolver resourcePatternResolver;

    @Autowired
    public YamlEvaluationCaseRepository(ObjectMapper objectMapper) {
        this("evaluation/case-sets/", objectMapper, new PathMatchingResourcePatternResolver());
    }

    YamlEvaluationCaseRepository(
            String caseSetsPrefix,
            ObjectMapper objectMapper,
            ResourcePatternResolver resourcePatternResolver) {
        this.caseSetsPrefix = caseSetsPrefix.endsWith("/") ? caseSetsPrefix : caseSetsPrefix + "/";
        this.objectMapper = objectMapper;
        this.resourcePatternResolver = resourcePatternResolver;
    }

    @Override
    public String getDefaultCaseSetId() {
        return DEFAULT_CASE_SET_ID;
    }

    @Override
    public EvaluationCaseSet loadCaseSet(String caseSetId) {
        Map<String, Object> manifest = loadYamlMap(caseSetId, MANIFEST_FILE);
        List<EvaluationCase> cases = loadCasesInternal(caseSetId, manifest);
        List<String> symptomGroups = stringList(manifest.get("symptom_groups"));
        if (symptomGroups.isEmpty()) {
            symptomGroups = cases.stream()
                    .map(EvaluationCase::symptomGroup)
                    .filter(group -> group != null && !group.isBlank())
                    .distinct()
                    .sorted()
                    .toList();
        }
        return new EvaluationCaseSet(
                requiredString(manifest, "case_set_id", caseSetId, MANIFEST_FILE),
                requiredString(manifest, "version", caseSetId, MANIFEST_FILE),
                symptomGroups,
                stringValue(manifest.get("asset_package_id")),
                stringValue(manifest.get("asset_package_version")),
                stringValue(manifest.get("description")),
                cases);
    }

    @Override
    public List<EvaluationCase> loadCases(String caseSetId) {
        Map<String, Object> manifest = loadYamlMap(caseSetId, MANIFEST_FILE);
        return List.copyOf(loadCasesInternal(caseSetId, manifest));
    }

    @Override
    public List<EvaluationCase> loadCasesBySymptomGroup(String caseSetId, String symptomGroup) {
        if (symptomGroup == null || symptomGroup.isBlank()) {
            throw new IllegalArgumentException("symptomGroup must not be blank");
        }
        return loadCases(caseSetId).stream()
                .filter(evaluationCase -> symptomGroup.equals(evaluationCase.symptomGroup()))
                .toList();
    }

    @Override
    public List<EvaluationCase> loadCasesByTag(String caseSetId, String tag) {
        if (tag == null || tag.isBlank()) {
            throw new IllegalArgumentException("tag must not be blank");
        }
        return loadCases(caseSetId).stream()
                .filter(evaluationCase -> evaluationCase.tags().contains(tag))
                .toList();
    }

    private List<EvaluationCase> loadCasesInternal(String caseSetId, Map<String, Object> manifest) {
        List<String> caseFiles = stringList(manifest.get("case_files"));
        if (caseFiles.isEmpty()) {
            throw new EvaluationLoadException(
                    "Case set manifest missing case_files: " + caseSetId,
                    caseSetId,
                    MANIFEST_FILE);
        }
        List<EvaluationCase> cases = new ArrayList<>();
        Set<String> caseIds = new LinkedHashSet<>();
        for (String caseFile : caseFiles) {
            Map<String, Object> caseFileRoot = loadYamlMap(caseSetId, caseFile);
            List<Map<String, Object>> rawCases = mapList(caseFileRoot.get("cases"), caseSetId, caseFile);
            for (Map<String, Object> rawCase : rawCases) {
                EvaluationCase evaluationCase = parseCase(rawCase, caseSetId, caseFile);
                if (!caseIds.add(evaluationCase.caseId())) {
                    throw new EvaluationLoadException(
                            "Duplicate case_id in case set: " + evaluationCase.caseId(),
                            caseSetId,
                            evaluationCase.caseId(),
                            caseFile,
                            null);
                }
                cases.add(evaluationCase);
            }
        }
        return cases;
    }

    private EvaluationCase parseCase(Map<String, Object> rawCase, String caseSetId, String resourcePath) {
        String caseId = stringValue(rawCase.get("case_id"));
        try {
            return objectMapper.convertValue(rawCase, EvaluationCase.class);
        } catch (IllegalArgumentException error) {
            throw new EvaluationLoadException(
                    "Invalid evaluation case format"
                            + (caseId == null ? "" : ": " + caseId)
                            + " - "
                            + error.getMessage(),
                    caseSetId,
                    caseId,
                    resourcePath,
                    error);
        } catch (IllegalStateException error) {
            throw new EvaluationLoadException(
                    "Invalid evaluation case format"
                            + (caseId == null ? "" : ": " + caseId)
                            + " - "
                            + error.getMessage(),
                    caseSetId,
                    caseId,
                    resourcePath,
                    error);
        }
    }

    private Map<String, Object> loadYamlMap(String caseSetId, String relativePath) {
        Resource resource = resolveResource(caseSetId, relativePath);
        if (!resource.exists()) {
            throw new EvaluationLoadException(
                    "Evaluation resource not found: " + buildLocation(caseSetId, relativePath),
                    caseSetId,
                    buildLocation(caseSetId, relativePath));
        }
        try (InputStream inputStream = resource.getInputStream()) {
            Object loaded = yaml.load(inputStream);
            if (!(loaded instanceof Map<?, ?> map)) {
                throw new EvaluationLoadException(
                        "Invalid YAML format, expected map: " + buildLocation(caseSetId, relativePath),
                        caseSetId,
                        buildLocation(caseSetId, relativePath));
            }
            return new LinkedHashMap<>((Map<String, Object>) map);
        } catch (EvaluationLoadException error) {
            throw error;
        } catch (IOException error) {
            throw new EvaluationLoadException(
                    "Failed to load evaluation resource: " + buildLocation(caseSetId, relativePath),
                    caseSetId,
                    null,
                    buildLocation(caseSetId, relativePath),
                    error);
        }
    }

    private Resource resolveResource(String caseSetId, String relativePath) {
        try {
            return resourcePatternResolver.getResource(
                    "classpath:" + buildLocation(caseSetId, normalizeRelativePath(relativePath)));
        } catch (Exception error) {
            throw new EvaluationLoadException(
                    "Failed to resolve evaluation resource: " + buildLocation(caseSetId, relativePath),
                    caseSetId,
                    null,
                    buildLocation(caseSetId, relativePath),
                    error);
        }
    }

    private String buildLocation(String caseSetId, String relativePath) {
        return caseSetsPrefix + caseSetId + "/" + normalizeRelativePath(relativePath);
    }

    private String normalizeRelativePath(String relativePath) {
        return relativePath.replace("\\", "/");
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> mapList(Object value, String caseSetId, String resourcePath) {
        if (!(value instanceof List<?> list)) {
            throw new EvaluationLoadException(
                    "Case file missing cases list: " + resourcePath,
                    caseSetId,
                    resourcePath);
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (!(item instanceof Map<?, ?> map)) {
                throw new EvaluationLoadException(
                        "Invalid case entry, expected map in " + resourcePath,
                        caseSetId,
                        resourcePath);
            }
            result.add(new LinkedHashMap<>((Map<String, Object>) map));
        }
        return result;
    }

    private String requiredString(
            Map<String, Object> root,
            String fieldName,
            String caseSetId,
            String resourcePath) {
        String value = stringValue(root.get(fieldName));
        if (value == null || value.isBlank()) {
            throw new EvaluationLoadException(
                    "Missing required field '" + fieldName + "' in " + resourcePath,
                    caseSetId,
                    resourcePath);
        }
        return value;
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    @SuppressWarnings("unchecked")
    private List<String> stringList(Object value) {
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
}
