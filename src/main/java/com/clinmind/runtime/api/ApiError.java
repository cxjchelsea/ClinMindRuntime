package com.clinmind.runtime.api;

public record ApiError(
        String code,
        String message
) {
}
