package com.clinmind.runtime.candidate.store;

public class CandidateNotFoundException extends RuntimeException {

    private final String generationId;
    private final String candidateId;
    private final CandidateResourceType resourceType;

    public CandidateNotFoundException(String message) {
        this(message, null, null, null);
    }

    public CandidateNotFoundException(String message, String generationId) {
        this(message, generationId, null, CandidateResourceType.GENERATION);
    }

    public CandidateNotFoundException(String message, String generationId, String candidateId) {
        this(message, generationId, candidateId, resolveLegacyType(generationId, candidateId, message));
    }

    public CandidateNotFoundException(
            String message, String generationId, String candidateId, CandidateResourceType resourceType) {
        super(message);
        this.generationId = generationId;
        this.candidateId = candidateId;
        this.resourceType = resourceType == null ? resolveLegacyType(generationId, candidateId, message) : resourceType;
    }

    public static CandidateNotFoundException generationNotFound(String generationId) {
        return new CandidateNotFoundException(
                "Candidate generation result not found: " + generationId,
                generationId,
                null,
                CandidateResourceType.GENERATION);
    }

    public static CandidateNotFoundException experienceCandidateNotFound(String candidateId) {
        return new CandidateNotFoundException(
                "Experience candidate not found: " + candidateId,
                null,
                candidateId,
                CandidateResourceType.EXPERIENCE_CANDIDATE);
    }

    public static CandidateNotFoundException trainingExampleCandidateNotFound(String candidateId) {
        return new CandidateNotFoundException(
                "Training example candidate not found: " + candidateId,
                null,
                candidateId,
                CandidateResourceType.TRAINING_EXAMPLE_CANDIDATE);
    }

    public String getGenerationId() {
        return generationId;
    }

    public String getCandidateId() {
        return candidateId;
    }

    public CandidateResourceType getResourceType() {
        return resourceType;
    }

    private static CandidateResourceType resolveLegacyType(
            String generationId, String candidateId, String message) {
        if (generationId != null) {
            return CandidateResourceType.GENERATION;
        }
        if (message != null && message.contains("Training example")) {
            return CandidateResourceType.TRAINING_EXAMPLE_CANDIDATE;
        }
        return CandidateResourceType.EXPERIENCE_CANDIDATE;
    }
}
