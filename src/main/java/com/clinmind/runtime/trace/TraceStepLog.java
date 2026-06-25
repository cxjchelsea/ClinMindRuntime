package com.clinmind.runtime.trace;

import java.time.Instant;

public record TraceStepLog(
        String runtimeId,
        String moduleName,
        String inputSummary,
        String outputSummary,
        Instant startTime,
        Instant endTime,
        long durationMs,
        boolean success,
        String errorMessage
) {
}
