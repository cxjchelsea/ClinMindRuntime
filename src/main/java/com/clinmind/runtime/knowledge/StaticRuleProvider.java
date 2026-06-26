package com.clinmind.runtime.knowledge;

import com.clinmind.runtime.state.CandidateStatus;
import com.clinmind.runtime.state.DiagnosisRef;
import com.clinmind.runtime.state.RedFlagRule;
import com.clinmind.runtime.state.RiskLevel;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

public class StaticRuleProvider {

    private final String assetsPrefix;
    private final Yaml yaml = new Yaml();

    public StaticRuleProvider() {
        this("assets/");
    }

    public StaticRuleProvider(String assetsPrefix) {
        this.assetsPrefix = assetsPrefix.endsWith("/") ? assetsPrefix : assetsPrefix + "/";
    }

    public SymptomGroupRule loadSymptomGroupRules(String symptomGroup) {
        String path = resolveSymptomGroupPath(symptomGroup);
        if (path == null) {
            return null;
        }
        Map<String, Object> root = loadYamlRequired(path);
        return new SymptomGroupRule(
                stringValue(root.get("symptom_group")),
                parseDiagnosisRefs(root.get("common_diagnoses")),
                parseDiagnosisRefs(root.get("must_not_miss")),
                stringList(root.get("required_questions")),
                stringList(root.get("recommended_tests")));
    }

    public List<RedFlagRule> loadRedFlagRules(String symptomGroup) {
        Map<String, Object> root = loadYamlRequired("red-flag-rules.yml");
        List<Map<String, Object>> rules = mapList(root.get("red_flag_rules"));
        List<RedFlagRule> result = new ArrayList<>();
        for (Map<String, Object> item : rules) {
            String group = stringValue(item.get("symptom_group"));
            if (symptomGroup != null && !symptomGroup.equals(group)) {
                continue;
            }
            result.add(new RedFlagRule(
                    stringValue(item.get("rule_id")),
                    group,
                    stringList(item.get("features")),
                    parseRiskLevel(item.get("risk_level")),
                    stringValue(item.get("action")),
                    stringValue(item.get("patient_constraint"))));
        }
        return result;
    }

    public List<TestRecommendationRule> loadTestRecommendationRules(String symptomGroup) {
        Map<String, Object> root = loadYamlRequired("test-recommendation-rules.yml");
        List<Map<String, Object>> rules = mapList(root.get("test_recommendation_rules"));
        List<TestRecommendationRule> result = new ArrayList<>();
        for (Map<String, Object> item : rules) {
            String group = stringValue(item.get("symptom_group"));
            if (symptomGroup != null && !symptomGroup.equals(group)) {
                continue;
            }
            result.add(new TestRecommendationRule(
                    stringValue(item.get("rule_id")),
                    group,
                    parseCandidateStatus(item.get("target_status")),
                    stringList(item.get("recommended_tests")),
                    stringValue(item.get("purpose"))));
        }
        return result;
    }

    public CapabilityProfile loadCapabilityProfile(String symptomGroup) {
        Map<String, Object> root = loadYamlRequired("capability-profiles.yml");
        for (Map<String, Object> item : mapList(root.get("capability_profiles"))) {
            if (symptomGroup != null && symptomGroup.equals(stringValue(item.get("symptom_group")))) {
                return new CapabilityProfile(
                        symptomGroup,
                        stringValue(item.get("level")),
                        stringList(item.get("patient_allowed_outputs")),
                        stringList(item.get("clinician_allowed_outputs")));
            }
        }
        return null;
    }

    private String resolveSymptomGroupPath(String symptomGroup) {
        if (symptomGroup == null) {
            return null;
        }
        return switch (symptomGroup) {
            case "chest_pain" -> "symptom-groups/chest-pain.yml";
            case "fever" -> "symptom-groups/fever.yml";
            default -> null;
        };
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> loadYamlRequired(String relativePath) {
        ClassPathResource resource = new ClassPathResource(assetsPrefix + relativePath);
        if (!resource.exists()) {
            throw new StaticRuleLoadException("Rule asset not found: " + assetsPrefix + relativePath);
        }
        try (InputStream inputStream = resource.getInputStream()) {
            Object loaded = yaml.load(inputStream);
            if (loaded instanceof Map<?, ?> map) {
                return (Map<String, Object>) map;
            }
            throw new StaticRuleLoadException("Invalid rule asset format: " + assetsPrefix + relativePath);
        } catch (StaticRuleLoadException error) {
            throw error;
        } catch (Exception error) {
            throw new StaticRuleLoadException(
                    "Failed to load rule asset: " + assetsPrefix + relativePath,
                    error);
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> mapList(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add((Map<String, Object>) map);
            }
        }
        return result;
    }

    private List<DiagnosisRef> parseDiagnosisRefs(Object value) {
        List<Map<String, Object>> items = mapList(value);
        List<DiagnosisRef> result = new ArrayList<>();
        for (Map<String, Object> item : items) {
            result.add(new DiagnosisRef(
                    stringValue(item.get("name")),
                    parseRiskLevel(item.get("risk_level"))));
        }
        return result;
    }

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
        return Collections.unmodifiableList(result);
    }

    private String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private RiskLevel parseRiskLevel(Object value) {
        if (value == null) {
            return RiskLevel.UNKNOWN;
        }
        return RiskLevel.fromValue(String.valueOf(value).toLowerCase());
    }

    private CandidateStatus parseCandidateStatus(Object value) {
        if (value == null) {
            return CandidateStatus.NEED_TO_RULE_OUT;
        }
        String normalized = String.valueOf(value).toLowerCase();
        return CandidateStatus.fromValue(normalized);
    }
}
