package com.clinmind.runtime.candidate.sanitization;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.SanitizationStatus;
import com.clinmind.runtime.candidate.TrainingTaskType;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateSanitizerTest {

    private CandidateSanitizer sanitizer;
    private CandidateSanitizationPolicy defaultPolicy;

    @BeforeEach
    void setUp() {
        defaultPolicy = CandidateSanitizationPolicy.defaults();
        sanitizer = new CandidateSanitizer(defaultPolicy);
    }

    @Test
    void retainsAndTruncatesSyntheticInputTexts() {
        String longText = "x".repeat(400);
        Map<String, Object> raw = Map.of("input_texts", List.of(longText));

        CandidateSanitizationResult result = sanitizer.sanitize(
                raw, TrainingTaskType.RISK_SIGNAL_CLASSIFICATION, CandidateInputSourceType.SYNTHETIC_EVALUATION);

        @SuppressWarnings("unchecked")
        List<String> texts = (List<String>) result.sanitizedInput().get("input_texts");
        assertThat(texts).hasSize(1);
        assertThat(texts.get(0)).hasSize(300);
        assertThat(result.sanitizationStatus()).isEqualTo(SanitizationStatus.NEEDS_REVIEW);
        assertThat(result.policyId()).isEqualTo("phase4-p1-default");
    }

    @Test
    void dropsInputTextsForRealRuntimeSource() {
        Map<String, Object> raw = Map.of("input_texts", List.of("真实患者主诉"));

        CandidateSanitizationResult result = sanitizer.sanitize(
                raw, TrainingTaskType.RISK_SIGNAL_CLASSIFICATION, CandidateInputSourceType.REAL_RUNTIME);

        assertThat(result.sanitizedInput()).doesNotContainKey("input_texts");
        assertThat(result.warnings()).anyMatch(w -> w.contains("Dropped input_texts"));
        assertThat(result.sanitizationStatus()).isEqualTo(SanitizationStatus.NEEDS_REVIEW);
    }

    @Test
    void dropsInputTextsForUnknownSource() {
        Map<String, Object> raw = Map.of("input_texts", List.of("unknown source text"));

        CandidateSanitizationResult result = sanitizer.sanitize(
                raw, TrainingTaskType.RISK_SIGNAL_CLASSIFICATION, CandidateInputSourceType.UNKNOWN);

        assertThat(result.sanitizedInput()).doesNotContainKey("input_texts");
        assertThat(result.sanitizationStatus()).isEqualTo(SanitizationStatus.NEEDS_REVIEW);
    }

    @Test
    void masksBasicInfoToAgeBucketAndSexOnly() {
        Map<String, Object> basicInfo = new HashMap<>();
        basicInfo.put("age", 58);
        basicInfo.put("sex", "male");
        basicInfo.put("name", "Patient A");
        basicInfo.put("phone", "13800000000");
        Map<String, Object> raw = Map.of("basic_info", basicInfo);

        CandidateSanitizationResult result = sanitizer.sanitize(
                raw, TrainingTaskType.RISK_SIGNAL_CLASSIFICATION, CandidateInputSourceType.SYNTHETIC_EVALUATION);

        @SuppressWarnings("unchecked")
        Map<String, Object> sanitizedBasicInfo = (Map<String, Object>) result.sanitizedInput().get("basic_info");
        assertThat(sanitizedBasicInfo).containsEntry("sex", "male");
        assertThat(sanitizedBasicInfo).containsEntry("age_bucket", "50-59");
        assertThat(sanitizedBasicInfo).doesNotContainKey("age");
        assertThat(sanitizedBasicInfo).doesNotContainKey("name");
        assertThat(sanitizedBasicInfo).doesNotContainKey("phone");
        assertThat(result.warnings()).anyMatch(w -> w.contains("Dropped basic_info field"));
    }

    @Test
    void dropsPatientOutputForNonSafeRewriteTask() {
        Map<String, Object> raw = Map.of("patient_output", "unsafe diagnosis text");

        CandidateSanitizationResult result = sanitizer.sanitize(
                raw, TrainingTaskType.RISK_SIGNAL_CLASSIFICATION, CandidateInputSourceType.SYNTHETIC_EVALUATION);

        assertThat(result.sanitizedInput()).doesNotContainKey("patient_output");
        assertThat(result.warnings()).anyMatch(w -> w.contains("Dropped patient_output"));
    }

    @Test
    void keepsTruncatedPatientOutputForPatientSafeRewrite() {
        String longOutput = "o".repeat(400);
        Map<String, Object> raw = Map.of("patient_output", longOutput);

        CandidateSanitizationResult result = sanitizer.sanitize(
                raw, TrainingTaskType.PATIENT_SAFE_REWRITE, CandidateInputSourceType.SYNTHETIC_EVALUATION);

        assertThat(result.sanitizedInput()).containsEntry("patient_output", longOutput.substring(0, 300));
    }

    @Test
    void blockedFieldSetsRejectedForPrivacy() {
        Map<String, Object> raw = Map.of("name", "Patient A", "input_texts", List.of("symptom"));

        CandidateSanitizationResult result = sanitizer.sanitize(
                raw, TrainingTaskType.RISK_SIGNAL_CLASSIFICATION, CandidateInputSourceType.SYNTHETIC_EVALUATION);

        assertThat(result.sanitizedInput()).doesNotContainKey("name");
        assertThat(result.sanitizationStatus()).isEqualTo(SanitizationStatus.REJECTED_FOR_PRIVACY);
        assertThat(result.warnings()).anyMatch(w -> w.contains("Dropped blocked field"));
    }

    @Test
    void addsInputSourceTypeToSanitizedInput() {
        CandidateSanitizationResult result = sanitizer.sanitize(
                Map.of(), TrainingTaskType.RISK_SIGNAL_CLASSIFICATION, CandidateInputSourceType.SYNTHETIC_EVALUATION);

        assertThat(result.sanitizedInput()).containsEntry("input_source_type", "SYNTHETIC_EVALUATION");
    }

    @Test
    void customPolicyCanAllowRealInputTexts() {
        CandidateSanitizationPolicy permissive = new CandidateSanitizationPolicy(
                "permissive",
                "1.0",
                true,
                true,
                false,
                false,
                true,
                50,
                List.of(),
                Map.of());
        CandidateSanitizer permissiveSanitizer = new CandidateSanitizer(permissive);
        Map<String, Object> raw = Map.of("input_texts", List.of("x".repeat(100)));

        CandidateSanitizationResult result = permissiveSanitizer.sanitize(
                raw, TrainingTaskType.RISK_SIGNAL_CLASSIFICATION, CandidateInputSourceType.REAL_RUNTIME);

        @SuppressWarnings("unchecked")
        List<String> texts = (List<String>) result.sanitizedInput().get("input_texts");
        assertThat(texts.get(0)).hasSize(50);
    }
}
