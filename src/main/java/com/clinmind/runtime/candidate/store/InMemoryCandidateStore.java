package com.clinmind.runtime.candidate.store;

import com.clinmind.runtime.candidate.CandidateGenerationResult;
import com.clinmind.runtime.candidate.ExperienceCandidate;
import com.clinmind.runtime.candidate.TrainingExampleCandidate;
import java.util.ArrayList;
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
            throw CandidateNotFoundException.generationNotFound(generationId);
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
            throw CandidateNotFoundException.experienceCandidateNotFound(candidateId);
        }
        return candidate;
    }

    @Override
    public TrainingExampleCandidate getTrainingExampleCandidate(String candidateId) {
        TrainingExampleCandidate candidate = trainingExampleCandidates.get(candidateId);
        if (candidate == null) {
            throw CandidateNotFoundException.trainingExampleCandidateNotFound(candidateId);
        }
        return candidate;
    }

    @Override
    public void updateExperienceCandidate(ExperienceCandidate candidate) {
        if (!experienceCandidates.containsKey(candidate.candidateId())) {
            throw CandidateNotFoundException.experienceCandidateNotFound(candidate.candidateId());
        }
        experienceCandidates.put(candidate.candidateId(), candidate);
        refreshGenerationResults(candidate.candidateId(), candidate, null);
    }

    @Override
    public void updateTrainingExampleCandidate(TrainingExampleCandidate candidate) {
        if (!trainingExampleCandidates.containsKey(candidate.candidateId())) {
            throw CandidateNotFoundException.trainingExampleCandidateNotFound(candidate.candidateId());
        }
        trainingExampleCandidates.put(candidate.candidateId(), candidate);
        refreshGenerationResults(candidate.candidateId(), null, candidate);
    }

    private void refreshGenerationResults(
            String candidateId, ExperienceCandidate experienceCandidate, TrainingExampleCandidate trainingCandidate) {
        for (Map.Entry<String, CandidateGenerationResult> entry : generationResults.entrySet()) {
            CandidateGenerationResult result = entry.getValue();
            boolean updated = false;
            List<ExperienceCandidate> experienceCandidates = result.experienceCandidates();
            List<TrainingExampleCandidate> trainingCandidates = result.trainingExampleCandidates();

            if (experienceCandidate != null) {
                List<ExperienceCandidate> refreshedExperience = new ArrayList<>();
                for (ExperienceCandidate candidate : experienceCandidates) {
                    if (candidate.candidateId().equals(candidateId)) {
                        refreshedExperience.add(experienceCandidate);
                        updated = true;
                    } else {
                        refreshedExperience.add(candidate);
                    }
                }
                experienceCandidates = refreshedExperience;
            }

            if (trainingCandidate != null) {
                List<TrainingExampleCandidate> refreshedTraining = new ArrayList<>();
                for (TrainingExampleCandidate candidate : trainingCandidates) {
                    if (candidate.candidateId().equals(candidateId)) {
                        refreshedTraining.add(trainingCandidate);
                        updated = true;
                    } else {
                        refreshedTraining.add(candidate);
                    }
                }
                trainingCandidates = refreshedTraining;
            }

            if (updated) {
                generationResults.put(
                        entry.getKey(),
                        new CandidateGenerationResult(
                                result.generationId(),
                                result.sourceEvaluationRunId(),
                                result.startedAt(),
                                result.completedAt(),
                                List.copyOf(experienceCandidates),
                                List.copyOf(trainingCandidates),
                                result.skippedItems(),
                                result.warnings()));
            }
        }
    }
}
