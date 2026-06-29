package com.clinmind.runtime.candidate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

class CandidateSourceRefTest {

    @Test
    void buildsSampleSourceRef() {
        CandidateSourceRef sourceRef = CandidateTestFixtures.sampleSourceRef();

        assertThat(sourceRef.sourceType()).isEqualTo(CandidateSourceType.SAFETY_VIOLATION);
        assertThat(sourceRef.evaluationRunId()).isEqualTo("eval_run_001");
        assertThat(sourceRef.assetPackageId()).isEqualTo("phase2-default");
    }

    @Test
    void rejectsMissingSourceType() {
        assertThatThrownBy(() -> new CandidateSourceRef(
                        null,
                        "rt_sample001",
                        "eval_run_001",
                        "case_001",
                        null,
                        null,
                        null,
                        null,
                        null,
                        "phase2-default",
                        "0.2.0",
                        "evaluation"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("sourceType");
    }
}
