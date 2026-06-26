package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EvaluationRunConfigTest {

    @Test
    void defaultsUseNonFailFast() {
        EvaluationRunConfig config = EvaluationRunConfig.defaults("phase3-default", "0.3.0");

        assertThat(config.caseSetId()).isEqualTo("phase3-default");
        assertThat(config.failFast()).isFalse();
        assertThat(config.baselineRunId()).isNull();
    }

    @Test
    void sampleConfigReferencesAssetPackage() {
        EvaluationRunConfig config = EvaluationTestFixtures.sampleRunConfig();

        assertThat(config.assetPackageId()).isEqualTo("phase2-default");
        assertThat(config.assetPackageVersion()).isEqualTo("0.2.0");
    }
}
