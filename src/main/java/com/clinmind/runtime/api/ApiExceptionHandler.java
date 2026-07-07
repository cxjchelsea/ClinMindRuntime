package com.clinmind.runtime.api;

import com.clinmind.runtime.agent.api.AgentAccessDeniedException;
import com.clinmind.runtime.agent.api.AgentExecutionNotFoundException;
import com.clinmind.runtime.candidate.generation.CandidateGenerationException;
import com.clinmind.runtime.candidate.review.CandidateReviewException;
import com.clinmind.runtime.candidate.sourceref.CandidateSourceRefValidationException;
import com.clinmind.runtime.candidate.store.CandidateNotFoundException;
import com.clinmind.runtime.candidate.store.CandidateResourceType;
import com.clinmind.runtime.console.access.AccessDeniedException;
import com.clinmind.runtime.console.access.ActorContextRequiredException;
import com.clinmind.runtime.console.access.InvalidDebugRoleException;
import com.clinmind.runtime.evaluation.EvaluationLoadException;
import com.clinmind.runtime.modelgov.ModelGovernancePolicyException;
import com.clinmind.runtime.storage.RuntimeNotFoundException;
import com.clinmind.runtime.toolgov.ToolGovernancePolicyException;
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

    @ExceptionHandler(CandidateSourceRefValidationException.class)
    public ResponseEntity<ApiResponse<Void>> handleCandidateSourceRefValidation(
            CandidateSourceRefValidationException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(new ApiError(ex.getCode(), ex.getMessage())));
    }

    @ExceptionHandler(CandidateReviewException.class)
    public ResponseEntity<ApiResponse<Void>> handleCandidateReview(CandidateReviewException ex) {
        HttpStatus status = "CANDIDATE_REVIEW_NOT_FOUND".equals(ex.getCode())
                ? HttpStatus.NOT_FOUND
                : HttpStatus.BAD_REQUEST;
        return ResponseEntity.status(status)
                .body(ApiResponse.fail(new ApiError(ex.getCode(), ex.getMessage())));
    }

    @ExceptionHandler(CandidateNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleCandidateNotFound(CandidateNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new ApiError(resolveCandidateNotFoundCode(ex), ex.getMessage())));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.fail(new ApiError(ex.getCode(), ex.getMessage())));
    }

    @ExceptionHandler(ModelGovernancePolicyException.class)
    public ResponseEntity<ApiResponse<Void>> handleModelGovernancePolicy(ModelGovernancePolicyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(new ApiError("MODEL_GOVERNANCE_POLICY_REJECTED", ex.getMessage())));
    }

    @ExceptionHandler(ToolGovernancePolicyException.class)
    public ResponseEntity<ApiResponse<Void>> handleToolGovernancePolicy(ToolGovernancePolicyException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(new ApiError("TOOL_GOVERNANCE_POLICY_REJECTED", ex.getMessage())));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAccessDenied(AccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(new ApiError("ACCESS_DENIED", ex.getMessage())));
    }

    @ExceptionHandler(InvalidDebugRoleException.class)
    public ResponseEntity<ApiResponse<Void>> handleInvalidDebugRole(InvalidDebugRoleException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(new ApiError("INVALID_DEBUG_ROLE", ex.getMessage())));
    }

    @ExceptionHandler(ActorContextRequiredException.class)
    public ResponseEntity<ApiResponse<Void>> handleActorContextRequired(ActorContextRequiredException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(new ApiError("ACTOR_CONTEXT_REQUIRED", ex.getMessage())));
    }

    private static String resolveCandidateNotFoundCode(CandidateNotFoundException ex) {
        return switch (ex.getResourceType()) {
            case GENERATION -> "CANDIDATE_GENERATION_NOT_FOUND";
            case TRAINING_EXAMPLE_CANDIDATE -> "TRAINING_EXAMPLE_CANDIDATE_NOT_FOUND";
            case EXPERIENCE_CANDIDATE -> "EXPERIENCE_CANDIDATE_NOT_FOUND";
        };
    }

    @ExceptionHandler(AgentExecutionNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleAgentExecutionNotFound(AgentExecutionNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(new ApiError("AGENT_EXECUTION_NOT_FOUND", ex.getMessage())));
    }

    @ExceptionHandler(AgentAccessDeniedException.class)
    public ResponseEntity<ApiResponse<Void>> handleAgentAccessDenied(AgentAccessDeniedException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.fail(new ApiError("ACCESS_DENIED", ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(400)
                .body(ApiResponse.fail(new ApiError("INVALID_REQUEST", "input.text 不能为空")));
    }
}
