package com.clinmind.runtime.candidate.generation;

public class CandidateGenerationException extends RuntimeException {

    private final String evaluationRunId;

    public CandidateGenerationException(String message, String evaluationRunId) {
        super(message);
        this.evaluationRunId = evaluationRunId;
    }

    public String getEvaluationRunId() {
        return evaluationRunId;
    }
}
