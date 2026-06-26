package com.clinmind.runtime.evaluation.yaml;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationCaseSet;
import com.clinmind.runtime.evaluation.EvaluationLoadException;
import com.clinmind.runtime.state.RuntimeMode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

@SpringBootTest
class YamlEvaluationCaseRepositoryTest {

    @Autowired
    private YamlEvaluationCaseRepository repository;

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
    }

    @Test
    void loadsDefaultCaseSetManifest() {
        EvaluationCaseSet caseSet = repository.loadCaseSet(YamlEvaluationCaseRepository.DEFAULT_CASE_SET_ID);

        assertThat(caseSet.caseSetId()).isEqualTo("phase3-default");
        assertThat(caseSet.version()).isEqualTo("0.3.0");
        assertThat(caseSet.assetPackageId()).isEqualTo("phase2-default");
        assertThat(caseSet.assetPackageVersion()).isEqualTo("0.2.0");
        assertThat(caseSet.symptomGroups()).contains("chest_pain", "fever");
        assertThat(caseSet.cases()).hasSizeGreaterThanOrEqualTo(10);
    }

    @Test
    void loadsAllCaseFilesFromDefaultSet() {
        assertThat(repository.loadCases("phase3-default"))
                .extracting(EvaluationCase::caseId)
                .contains(
                        "chest_pain_high_risk_001",
                        "fever_normal_001",
                        "wellness_regression_001",
                        "unsupported_regression_001",
                        "patient_boundary_001",
                        "trace_asset_001");
    }

    @Test
    void unknownCaseSetThrows() {
        assertThatThrownBy(() -> repository.loadCaseSet("not-exist"))
                .isInstanceOf(EvaluationLoadException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void brokenCaseFormatThrowsExplicitError() {
        YamlEvaluationCaseRepository isolated = new YamlEvaluationCaseRepository(
                "evaluation/case-sets/",
                objectMapper,
                new PathMatchingResourcePatternResolver());

        assertThatThrownBy(() -> isolated.loadCases("phase3-test"))
                .isInstanceOf(EvaluationLoadException.class)
                .satisfies(error -> {
                    EvaluationLoadException loadError = (EvaluationLoadException) error;
                    assertThat(loadError.getCaseSetId()).isEqualTo("phase3-test");
                    assertThat(loadError.getCaseId()).isEqualTo("broken_case_001");
                    assertThat(loadError.getMessage()).contains("inputTurns");
                });
    }

    @Test
    void filtersCasesBySymptomGroup() {
        assertThat(repository.loadCasesBySymptomGroup("phase3-default", "fever"))
                .isNotEmpty()
                .allMatch(evaluationCase -> "fever".equals(evaluationCase.symptomGroup()));
    }

    @Test
    void filtersCasesByTag() {
        assertThat(repository.loadCasesByTag("phase3-default", "safety_gate"))
                .isNotEmpty()
                .allMatch(evaluationCase -> evaluationCase.tags().contains("safety_gate"));
    }

    @Test
    void parsedCasePreservesModeAndExpectedOutcome() {
        EvaluationCase evaluationCase = repository.loadCases("phase3-default").stream()
                .filter(item -> "chest_pain_high_risk_001".equals(item.caseId()))
                .findFirst()
                .orElseThrow();

        assertThat(evaluationCase.mode()).isEqualTo(RuntimeMode.PATIENT_FACING);
        assertThat(evaluationCase.expectedOutcome().safetyGateTriggered()).isTrue();
        assertThat(evaluationCase.expectedOutcome().requiredAssetTrace()).isTrue();
    }
}
