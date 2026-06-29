package com.clinmind.runtime.api;

import com.clinmind.runtime.candidate.generation.CandidateGenerationException;
import com.clinmind.runtime.candidate.store.CandidateNotFoundException;
import com.clinmind.runtime.evaluation.EvaluationLoadException;
import com.clinmind.runtime.storage.RuntimeNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(EvaluationLoadException.class)
    public ResponseEntity<ApiResponse<Void>> handleEvaluationLoad(EvaluationLoadException ex) {
        String code = ex.getMessage() != null && ex.getMessage().contains("case set")
                ? "EVALUATION_CASE_SET_NOT_FOUND"
                : "EVALUATION_RUN_NOT_FOUND";
        return ResponseEntity.status(404)
                .body(ApiResponse.fail(new ApiError(code, ex.getMessage())));
    }

    @ExceptionHandler(RuntimeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeNotFound(RuntimeNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail(new ApiError("RUNTIME_NOT_FOUND", "Runtime 不存在")));
    }

    @ExceptionHandler(CandidateGenerationException.class)
    public ResponseEntity<ApiResponse<Void>> handleCandidateGeneration(CandidateGenerationException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ApiResponse.fail(new ApiError("EVALUATION_RUN_NOT_COMPLETED", ex.getMessage())));
    }

    @ExceptionHandler(CandidateNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCandidateNotFound(CandidateNotFoundException ex) {
        String code = resolveCandidateNotFoundCode(ex);
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new ApiError(code, ex.getMessage())));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.fail(new ApiError(ex.getCode(), ex.getMessage())));
    }

    private static String resolveCandidateNotFoundCode(CandidateNotFoundException ex) {
        if (ex.getGenerationId() != null) {
            return "CANDIDATE_GENERATION_NOT_FOUND";
        }
        String message = ex.getMessage();
        if (message != null && message.contains("Training example")) {
            return "TRAINING_EXAMPLE_CANDIDATE_NOT_FOUND";
        }
        return "EXPERIENCE_CANDIDATE_NOT_FOUND";
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(400)
                .body(ApiResponse.fail(new ApiError("INVALID_REQUEST", "input.text 不能为空")));
    }
}
