package com.clinmind.runtime.candidate.review;

public class CandidateReviewException extends RuntimeException {

    private final String code;

    public CandidateReviewException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
