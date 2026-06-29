package com.clinmind.runtime.candidate.sourceref;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.candidate.CandidateSourceRef;
import com.clinmind.runtime.candidate.CandidateSourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CandidateSourceRefValidatorTest {

    private CandidateSourceRefValidator validator;

    @BeforeEach
    void setUp() {
        validator = new CandidateSourceRefValidator();
    }

    @Test
    void acceptsValidMetricResultSourceRef() {
        assertThatCode(() -> validator.validate(validMetricResultRef())).doesNotThrowAnyException();
    }

    @Test
    void rejectsMetricResultMissingEvaluationRunId() {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.METRIC_RESULT,
                "rt_001",
                null,
                "case_001",
                "item_001",
                "trace_001",
                null,
                null,
                "safety_gate",
                "phase2-default",
                "0.2.0",
                "metric_result");

        assertThatThrownBy(() -> validator.validate(sourceRef))
                .isInstanceOf(CandidateSourceRefValidationException.class)
                .extracting(ex -> ((CandidateSourceRefValidationException) ex).getCode())
                .isEqualTo("MISSING_CANDIDATE_SOURCE_FIELD");
    }

    @Test
    void rejectsSafetyViolationMissingViolationId() {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.SAFETY_VIOLATION,
                "rt_001",
                "eval_001",
                "case_001",
                "item_001",
                "trace_001",
                null,
                null,
                "safety_gate",
                "phase2-default",
                "0.2.0",
                "safety_violation");

        assertThatThrownBy(() -> validator.validate(sourceRef))
                .isInstanceOf(CandidateSourceRefValidationException.class);
    }

    @Test
    void rejectsRegressionFindingMissingFindingId() {
        CandidateSourceRef sourceRef = new CandidateSourceRef(
                CandidateSourceType.REGRESSION_FINDING,
                null,
                "eval_001",
                "case_001",
                null,
                null,
                null,
                null,
                null,
                "phase2-default",
                "0.2.0",
                "regression_finding");

        assertThatThrownBy(() -> validator.validate(sourceRef))
                .isInstanceOf(CandidateSourceRefValidationException.class);
    }

    private static CandidateSourceRef validMetricResultRef() {
        return new CandidateSourceRef(
                CandidateSourceType.METRIC_RESULT,
                "rt_001",
                "eval_001",
                "case_001",
                "item_001",
                "trace_001",
                null,
                null,
                "safety_gate",
                "phase2-default",
                "0.2.0",
                "metric_result");
    }
}
