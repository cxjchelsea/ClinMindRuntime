package com.clinmind.runtime.api;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ApiResponse<T>(
        boolean success,
        T data,
        ApiError error,
        @JsonProperty("trace_id") String traceId
) {
    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, data, null, null);
    }

    public static <T> ApiResponse<T> ok(T data, String traceId) {
        return new ApiResponse<>(true, data, null, traceId);
    }

    public static <T> ApiResponse<T> fail(ApiError error) {
        return new ApiResponse<>(false, null, error, null);
    }
}
