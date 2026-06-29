package com.clinmind.runtime.candidate.generation;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.store.CandidateStore;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunConfig;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationRunStore;
import com.clinmind.runtime.evaluation.RuntimeEvaluationRunner;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class CandidateGenerationServiceIntegrationTest {

    @Autowired
    private RuntimeEvaluationRunner evaluationRunner;

    @Autowired
    private EvaluationRunStore runStore;

    @Autowired
    private CandidateGenerationService candidateGenerationService;

    @Autowired
    private CandidateStore candidateStore;

    @Test
    void generatesCandidatesFromRealEvaluationRun() {
        EvaluationRun run = evaluationRunner.run(EvaluationRunConfig.defaults("phase3-default", "0.3.0"));

        assertThat(run.status()).isIn(
                EvaluationRunStatus.COMPLETED, EvaluationRunStatus.PARTIALLY_FAILED, EvaluationRunStatus.FAILED);

        var result = candidateGenerationService.generateFromEvaluationRun(run.runId(), CandidateGenerationPolicy.defaults());

        assertThat(result.sourceEvaluationRunId()).isEqualTo(run.runId());
        assertThat(candidateStore.getGenerationResult(result.generationId())).isEqualTo(result);
    }
}
