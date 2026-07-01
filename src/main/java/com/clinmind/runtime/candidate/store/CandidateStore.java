package com.clinmind.runtime.candidate.store;

import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.CandidateReviewStatus;
import com.clinmind.runtime.candidate.CandidateRiskLevel;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import com.clinmind.runtime.candidate.TrainingTaskType;
import java.util.List;

public interface CandidateStore {

    void saveGenerationResult(CandidateGenerationResult result);

    CandidateGenerationResult getGenerationResult(String generationId);

    List<CandidateGenerationResult> listGenerationResults(String sourceEvaluationRunId, int limit);

    List<ExperienceCandidate> listExperienceCandidates(String generationId);

    List<TrainingExampleCandidate> listTrainingExampleCandidates(String generationId);

    List<ExperienceCandidate> listExperienceCandidates(
            CandidateReviewStatus reviewStatus, CandidateRiskLevel riskLevel, int limit);

    List<TrainingExampleCandidate> listTrainingExampleCandidates(
            CandidateReviewStatus reviewStatus, CandidateRiskLevel riskLevel, TrainingTaskType taskType, int limit);

    ExperienceCandidate getExperienceCandidate(String candidateId);

    TrainingExampleCandidate getTrainingExampleCandidate(String candidateId);

    void updateExperienceCandidate(ExperienceCandidate candidate);

    void updateTrainingExampleCandidate(TrainingExampleCandidate candidate);
}
