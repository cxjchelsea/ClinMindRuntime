package com.clinmind.runtime.api;

import com.clinmind.runtime.storage.RuntimeNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(RuntimeNotFoundException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeNotFound(RuntimeNotFoundException ex) {
        return ResponseEntity.status(404)
                .body(ApiResponse.fail(new ApiError("RUNTIME_NOT_FOUND", "Runtime 不存在")));
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiResponse<Void>> handleApiException(ApiException ex) {
        return ResponseEntity.status(ex.getStatus())
                .body(ApiResponse.fail(new ApiError(ex.getCode(), ex.getMessage())));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        return ResponseEntity.status(400)
                .body(ApiResponse.fail(new ApiError("INVALID_REQUEST", "input.text 不能为空")));
    }
}
