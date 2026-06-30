package com.clinmind.runtime.candidate.generation;

import com.clinmind.runtime.candidate.CandidateGenerationPolicy;
import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.CandidateSkippedItem;
import com.clinmind.runtime.candidate.CandidateSkippedReason;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.audit.AuditResourceType;
import com.clinmind.runtime.audit.AuditResultStatus;
import com.clinmind.runtime.candidate.store.CandidateStore;
import com.clinmind.runtime.evaluation.EvaluationCase;
import com.clinmind.runtime.evaluation.EvaluationCaseRepository;
import com.clinmind.runtime.evaluation.EvaluationItemResult;
import com.clinmind.runtime.evaluation.EvaluationLoadException;
import com.clinmind.runtime.evaluation.EvaluationRun;
import com.clinmind.runtime.evaluation.EvaluationRunStatus;
import com.clinmind.runtime.evaluation.EvaluationRunStore;
import com.clinmind.runtime.evaluation.MetricResult;
import com.clinmind.runtime.evaluation.RuntimeCaseExecution;
import com.clinmind.runtime.state.IdGenerator;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CandidateGenerationService {

    private final EvaluationRunStore runStore;
    private final EvaluationCaseRepository caseRepository;
    private final ExperienceCandidateGenerator experienceCandidateGenerator;
    private final TrainingExampleCandidateGenerator trainingExampleCandidateGenerator;
    private final CandidateMappingPolicy mappingPolicy;
    private final CandidateStore candidateStore;
    private final AuditLogService auditLogService;

    public CandidateGenerationService(
            EvaluationRunStore runStore,
            EvaluationCaseRepository caseRepository,
            ExperienceCandidateGenerator experienceCandidateGenerator,
            TrainingExampleCandidateGenerator trainingExampleCandidateGenerator,
            CandidateMappingPolicy mappingPolicy,
            CandidateStore candidateStore,
            AuditLogService auditLogService) {
        this.runStore = runStore;
        this.caseRepository = caseRepository;
        this.experienceCandidateGenerator = experienceCandidateGenerator;
        this.trainingExampleCandidateGenerator = trainingExampleCandidateGenerator;
        this.mappingPolicy = mappingPolicy;
        this.candidateStore = candidateStore;
        this.auditLogService = auditLogService;
    }

    public CandidateGenerationResult generateFromEvaluationRun(String runId) {
        return generateFromEvaluationRun(runId, CandidateGenerationPolicy.defaults());
    }

    @Transactional
    public CandidateGenerationResult generateFromEvaluationRun(String runId, CandidateGenerationPolicy policy) {
        Instant startedAt = Instant.now();
        EvaluationRun run = runStore.get(runId);
        validateRunCompleted(run);

        CandidateGenerationPolicy effectivePolicy =
                policy == null ? CandidateGenerationPolicy.defaults() : policy;
        Map<String, EvaluationCase> casesById = loadCasesById(run);
        List<ExperienceCandidate> experienceCandidates = new ArrayList<>();
        List<TrainingExampleCandidate> trainingCandidates = new ArrayList<>();
        List<CandidateSkippedItem> skippedItems = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        if (run.result() != null && !run.result().majorFindings().isEmpty()) {
            experienceCandidates.addAll(experienceCandidateGenerator.generateFromRegressionFindings(
                    run, run.result().majorFindings(), effectivePolicy));
        }

        for (EvaluationItemResult itemResult : run.itemResults()) {
            EvaluationCase evaluationCase = casesById.get(itemResult.caseId());
            if (evaluationCase == null) {
                warnings.add("Evaluation case definition not found: " + itemResult.caseId());
            }

            RuntimeCaseExecution execution = resolveExecution(runId, itemResult, skippedItems);
            experienceCandidates.addAll(experienceCandidateGenerator.generateFromItemResult(
                    run, itemResult, evaluationCase, execution, effectivePolicy));
            trainingCandidates.addAll(trainingExampleCandidateGenerator.generateFromItemResult(
                    run, itemResult, evaluationCase, execution, effectivePolicy));
            recordMetricSkips(itemResult, effectivePolicy, skippedItems);
        }

        CandidateGenerationResult result = new CandidateGenerationResult(
                IdGenerator.candidateGenerationId(),
                runId,
                startedAt,
                Instant.now(),
                List.copyOf(experienceCandidates),
                List.copyOf(trainingCandidates),
                List.copyOf(skippedItems),
                List.copyOf(warnings));
        candidateStore.saveGenerationResult(result);
        auditLogService.record(
                AuditActionType.GENERATE_CANDIDATES,
                AuditResourceType.CANDIDATE_GENERATION,
                result.generationId(),
                AuditResultStatus.SUCCESS,
                Map.of(
                        "source_evaluation_run_id", runId,
                        "experience_candidate_count", result.experienceCandidates().size(),
                        "training_candidate_count", result.trainingExampleCandidates().size()));
        return result;
    }

    private RuntimeCaseExecution resolveExecution(
            String runId, EvaluationItemResult itemResult, List<CandidateSkippedItem> skippedItems) {
        try {
            return runStore.getExecution(runId, itemResult.caseId());
        } catch (EvaluationLoadException exception) {
            skippedItems.add(new CandidateSkippedItem(
                    itemResult.caseId(),
                    itemResult.runId() + ":" + itemResult.caseId(),
                    null,
                    CandidateSkippedReason.RUNTIME_CASE_EXECUTION_MISSING,
                    exception.getMessage()));
            return null;
        }
    }

    private void recordMetricSkips(
            EvaluationItemResult itemResult,
            CandidateGenerationPolicy policy,
            List<CandidateSkippedItem> skippedItems) {
        for (MetricResult metric : itemResult.metricResults()) {
            mappingPolicy
                    .resolveSkipReason(metric, policy, itemResult.passed())
                    .ifPresent(reason -> skippedItems.add(new CandidateSkippedItem(
                            itemResult.caseId(),
                            itemResult.runId() + ":" + itemResult.caseId(),
                            metric.metricId(),
                            reason,
                            metric.message())));
        }
    }

    private Map<String, EvaluationCase> loadCasesById(EvaluationRun run) {
        Map<String, EvaluationCase> casesById = new HashMap<>();
        if (run.config() == null || run.config().caseSetId() == null) {
            return casesById;
        }
        for (EvaluationCase evaluationCase : caseRepository.loadCases(run.config().caseSetId())) {
            casesById.put(evaluationCase.caseId(), evaluationCase);
        }
        return casesById;
    }

    private static void validateRunCompleted(EvaluationRun run) {
        if (run.status() == EvaluationRunStatus.RUNNING || run.status() == EvaluationRunStatus.CREATED) {
            throw new CandidateGenerationException(
                    "Evaluation run is not completed: " + run.runId() + " (" + run.status().getValue() + ")",
                    run.runId());
        }
    }
}
