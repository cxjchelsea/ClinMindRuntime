package com.clinmind.runtime.evaluation;

public class EvaluationLoadException extends RuntimeException {

    private final String caseSetId;
    private final String caseId;
    private final String resourcePath;

    public EvaluationLoadException(String message, String caseSetId) {
        this(message, caseSetId, null, null, null);
    }

    public EvaluationLoadException(String message, String caseSetId, String resourcePath) {
        this(message, caseSetId, null, resourcePath, null);
    }

    public EvaluationLoadException(
            String message,
            String caseSetId,
            String caseId,
            String resourcePath,
            Throwable cause) {
        super(message, cause);
        this.caseSetId = caseSetId;
        this.caseId = caseId;
        this.resourcePath = resourcePath;
    }

    public String getCaseSetId() {
        return caseSetId;
    }

    public String getCaseId() {
        return caseId;
    }

    public String getResourcePath() {
        return resourcePath;
    }
}
