package com.clinmind.runtime.candidate.store;

import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import java.util.List;

public interface CandidateStore {

    void saveGenerationResult(CandidateGenerationResult result);

    CandidateGenerationResult getGenerationResult(String generationId);

    List<ExperienceCandidate> listExperienceCandidates(String generationId);

    List<TrainingExampleCandidate> listTrainingExampleCandidates(String generationId);

    ExperienceCandidate getExperienceCandidate(String candidateId);

    TrainingExampleCandidate getTrainingExampleCandidate(String candidateId);

    void updateExperienceCandidate(ExperienceCandidate candidate);

    void updateTrainingExampleCandidate(TrainingExampleCandidate candidate);
}
