package com.clinmind.runtime.evaluation;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class EvaluationCaseSetTest {

    @Test
    void buildsSampleCaseSet() {
        EvaluationCaseSet caseSet = EvaluationTestFixtures.sampleCaseSet();

        assertThat(caseSet.caseSetId()).isEqualTo("phase3-default");
        assertThat(caseSet.version()).isEqualTo("0.3.0");
        assertThat(caseSet.assetPackageId()).isEqualTo("phase2-default");
        assertThat(caseSet.cases()).hasSize(1);
    }
}
