package com.clinmind.runtime.candidate.store;

public class CandidateNotFoundException extends RuntimeException {

    private final String generationId;
    private final String candidateId;

    public CandidateNotFoundException(String message) {
        this(message, null, null);
    }

    public CandidateNotFoundException(String message, String generationId) {
        this(message, generationId, null);
    }

    public CandidateNotFoundException(String message, String generationId, String candidateId) {
        super(message);
        this.generationId = generationId;
        this.candidateId = candidateId;
    }

    public String getGenerationId() {
        return generationId;
    }

    public String getCandidateId() {
        return candidateId;
    }
}
