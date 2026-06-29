package com.clinmind.runtime.candidate.store;

import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

@Component
public class InMemoryCandidateStore implements CandidateStore {

    private final Map<String, CandidateGenerationResult> generationResults = new ConcurrentHashMap<>();
    private final Map<String, ExperienceCandidate> experienceCandidates = new ConcurrentHashMap<>();
    private final Map<String, TrainingExampleCandidate> trainingExampleCandidates = new ConcurrentHashMap<>();

    @Override
    public void saveGenerationResult(CandidateGenerationResult result) {
        generationResults.put(result.generationId(), result);
        result.experienceCandidates().forEach(candidate -> experienceCandidates.put(candidate.candidateId(), candidate));
        result.trainingExampleCandidates()
                .forEach(candidate -> trainingExampleCandidates.put(candidate.candidateId(), candidate));
    }

    @Override
    public CandidateGenerationResult getGenerationResult(String generationId) {
        CandidateGenerationResult result = generationResults.get(generationId);
        if (result == null) {
            throw new CandidateNotFoundException("Candidate generation result not found: " + generationId, generationId);
        }
        return result;
    }

    @Override
    public List<ExperienceCandidate> listExperienceCandidates(String generationId) {
        return getGenerationResult(generationId).experienceCandidates();
    }

    @Override
    public List<TrainingExampleCandidate> listTrainingExampleCandidates(String generationId) {
        return getGenerationResult(generationId).trainingExampleCandidates();
    }

    @Override
    public ExperienceCandidate getExperienceCandidate(String candidateId) {
        ExperienceCandidate candidate = experienceCandidates.get(candidateId);
        if (candidate == null) {
            throw new CandidateNotFoundException("Experience candidate not found: " + candidateId, null, candidateId);
        }
        return candidate;
    }

    @Override
    public TrainingExampleCandidate getTrainingExampleCandidate(String candidateId) {
        TrainingExampleCandidate candidate = trainingExampleCandidates.get(candidateId);
        if (candidate == null) {
            throw new CandidateNotFoundException(
                    "Training example candidate not found: " + candidateId,
                    null,
                    candidateId);
        }
        return candidate;
    }
}
