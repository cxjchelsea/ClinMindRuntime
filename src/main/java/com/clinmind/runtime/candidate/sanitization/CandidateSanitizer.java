package com.clinmind.runtime.candidate.sanitization;

import com.clinmind.runtime.candidate.SanitizationStatus;
import com.clinmind.runtime.candidate.TrainingTaskType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class CandidateSanitizer {

    private static final Set<String> ALLOWED_BASIC_INFO_KEYS = Set.of("age_bucket", "sex");

    private final CandidateSanitizationPolicy defaultPolicy;

    public CandidateSanitizer() {
        this(CandidateSanitizationPolicy.defaults());
    }

    public CandidateSanitizer(CandidateSanitizationPolicy defaultPolicy) {
        this.defaultPolicy = defaultPolicy == null ? CandidateSanitizationPolicy.defaults() : defaultPolicy;
    }

    public CandidateSanitizationResult sanitize(
            Map<String, Object> rawInput,
            TrainingTaskType taskType,
            CandidateInputSourceType sourceType) {
        return sanitize(rawInput, taskType, sourceType, defaultPolicy);
    }

    public CandidateSanitizationResult sanitize(
            Map<String, Object> rawInput,
            TrainingTaskType taskType,
            CandidateInputSourceType sourceType,
            CandidateSanitizationPolicy policy) {
        CandidateSanitizationPolicy effectivePolicy = policy == null ? defaultPolicy : policy;
        Map<String, Object> sanitized = new HashMap<>();
        List<String> warnings = new ArrayList<>();
        boolean modified = false;
        boolean rejected = false;

        if (rawInput != null) {
            for (Map.Entry<String, Object> entry : rawInput.entrySet()) {
                String field = entry.getKey();
                if (effectivePolicy.blockedFields().contains(field)) {
                    warnings.add("Dropped blocked field: " + field);
                    modified = true;
                    rejected = true;
                    continue;
                }

                switch (field) {
                    case "input_texts" -> {
                        Object sanitizedTexts = sanitizeInputTexts(entry.getValue(), sourceType, effectivePolicy, warnings);
                        if (sanitizedTexts != null) {
                            sanitized.put(field, sanitizedTexts);
                            if (!sanitizedTexts.equals(entry.getValue())) {
                                modified = true;
                            }
                        } else {
                            modified = true;
                        }
                    }
                    case "basic_info" -> {
                        Object sanitizedBasicInfo = sanitizeBasicInfo(entry.getValue(), effectivePolicy, warnings);
                        if (sanitizedBasicInfo != null) {
                            sanitized.put(field, sanitizedBasicInfo);
                            if (!sanitizedBasicInfo.equals(entry.getValue())) {
                                modified = true;
                            }
                        } else {
                            modified = true;
                        }
                    }
                    case "patient_output" -> {
                        if (shouldKeepPatientOutput(taskType, effectivePolicy)) {
                            Object truncated = truncateValue(entry.getValue(), effectivePolicy.maxInputTextLength());
                            sanitized.put(field, truncated);
                            if (!truncated.equals(entry.getValue())) {
                                modified = true;
                            }
                        } else {
                            warnings.add("Dropped patient_output");
                            modified = true;
                        }
                    }
                    case "patient_output_level" -> {
                        warnings.add("Dropped patient_output_level");
                        modified = true;
                    }
                    case "case_frame_summary" -> {
                        Object summarized = summarizeCaseFrame(entry.getValue(), effectivePolicy.maxInputTextLength(), warnings);
                        sanitized.put(field, summarized);
                        if (!summarized.equals(entry.getValue())) {
                            modified = true;
                        }
                    }
                    default -> {
                        sanitized.put(field, truncateStructuredValue(entry.getValue(), effectivePolicy.maxInputTextLength()));
                    }
                }
            }
        }

        sanitized.put("input_source_type", sourceType.name());

        SanitizationStatus status = resolveStatus(sourceType, modified, rejected);
        return new CandidateSanitizationResult(
                Map.copyOf(sanitized),
                status,
                List.copyOf(warnings),
                effectivePolicy.policyId(),
                effectivePolicy.policyVersion());
    }

    private static SanitizationStatus resolveStatus(
            CandidateInputSourceType sourceType, boolean modified, boolean rejected) {
        if (rejected) {
            return SanitizationStatus.REJECTED_FOR_PRIVACY;
        }
        if (sourceType == CandidateInputSourceType.UNKNOWN || modified) {
            return SanitizationStatus.NEEDS_REVIEW;
        }
        return SanitizationStatus.SANITIZED;
    }

    private static Object sanitizeInputTexts(
            Object value,
            CandidateInputSourceType sourceType,
            CandidateSanitizationPolicy policy,
            List<String> warnings) {
        if (!(value instanceof List<?> texts)) {
            return null;
        }
        boolean allow = switch (sourceType) {
            case SYNTHETIC_EVALUATION -> policy.allowSyntheticInputTexts();
            case REAL_RUNTIME -> policy.allowRealInputTexts();
            case UNKNOWN -> false;
        };
        if (!allow) {
            warnings.add("Dropped input_texts for source type: " + sourceType);
            return null;
        }
        List<String> sanitizedTexts = new ArrayList<>();
        for (Object text : texts) {
            if (text == null) {
                continue;
            }
            sanitizedTexts.add(truncateText(String.valueOf(text), policy.maxInputTextLength()));
        }
        return List.copyOf(sanitizedTexts);
    }

    private static Object sanitizeBasicInfo(
            Object value, CandidateSanitizationPolicy policy, List<String> warnings) {
        if (!(value instanceof Map<?, ?> basicInfo)) {
            return null;
        }
        if (!policy.maskBasicInfo()) {
            return Map.copyOf(basicInfo);
        }
        Map<String, Object> sanitized = new HashMap<>();
        for (String key : ALLOWED_BASIC_INFO_KEYS) {
            if (basicInfo.containsKey(key)) {
                sanitized.put(key, basicInfo.get(key));
            }
        }
        if (basicInfo.containsKey("age") && !sanitized.containsKey("age_bucket")) {
            sanitized.put("age_bucket", toAgeBucket(basicInfo.get("age")));
            warnings.add("Converted basic_info.age to age_bucket");
        }
        for (Object key : basicInfo.keySet()) {
            String field = String.valueOf(key);
            if (!ALLOWED_BASIC_INFO_KEYS.contains(field) && !"age".equals(field)) {
                warnings.add("Dropped basic_info field: " + field);
            }
        }
        return Map.copyOf(sanitized);
    }

    private static Object summarizeCaseFrame(Object value, int maxLength, List<String> warnings) {
        if (!(value instanceof Map<?, ?> summary)) {
            return value;
        }
        Map<String, Object> sanitized = new HashMap<>();
        for (Map.Entry<?, ?> entry : summary.entrySet()) {
            sanitized.put(String.valueOf(entry.getKey()), truncateStructuredValue(entry.getValue(), maxLength));
        }
        if (!sanitized.equals(summary)) {
            warnings.add("Truncated case_frame_summary fields");
        }
        return Map.copyOf(sanitized);
    }

    private static boolean shouldKeepPatientOutput(TrainingTaskType taskType, CandidateSanitizationPolicy policy) {
        return taskType == TrainingTaskType.PATIENT_SAFE_REWRITE && policy.allowPatientOutputForSafeRewrite();
    }

    private static Object truncateStructuredValue(Object value, int maxLength) {
        if (value instanceof String text) {
            return truncateText(text, maxLength);
        }
        if (value instanceof List<?> list) {
            return list.stream().map(item -> truncateStructuredValue(item, maxLength)).toList();
        }
        if (value instanceof Map<?, ?> map) {
            Map<String, Object> copy = new HashMap<>();
            map.forEach((key, item) -> copy.put(String.valueOf(key), truncateStructuredValue(item, maxLength)));
            return Map.copyOf(copy);
        }
        return value;
    }

    private static Object truncateValue(Object value, int maxLength) {
        if (value instanceof String text) {
            return truncateText(text, maxLength);
        }
        return value;
    }

    private static String truncateText(String text, int maxLength) {
        if (text == null || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength);
    }

    private static String toAgeBucket(Object ageValue) {
        if (ageValue instanceof Number number) {
            int age = number.intValue();
            int lower = (age / 10) * 10;
            return lower + "-" + (lower + 9);
        }
        return String.valueOf(ageValue);
    }
}
