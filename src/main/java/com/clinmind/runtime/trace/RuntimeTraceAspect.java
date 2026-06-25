package com.clinmind.runtime.trace;

import com.clinmind.runtime.state.RuntimeState;
import java.time.Duration;
import java.time.Instant;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class RuntimeTraceAspect {

    private final RuntimeTraceCollector collector;

    public RuntimeTraceAspect(RuntimeTraceCollector collector) {
        this.collector = collector;
    }

    @Around("@annotation(traceStep)")
    public Object traceExecution(ProceedingJoinPoint joinPoint, TraceStep traceStep) throws Throwable {
        Instant startTime = Instant.now();
        String moduleName = traceStep.value();
        Object[] args = joinPoint.getArgs();
        String runtimeId = resolveRuntimeId(args);
        String inputSummary = summarizeArgs(args);

        try {
            Object result = joinPoint.proceed();
            Instant endTime = Instant.now();
            collector.record(new TraceStepLog(
                    runtimeId,
                    moduleName,
                    inputSummary,
                    summarizeResult(result),
                    startTime,
                    endTime,
                    Duration.between(startTime, endTime).toMillis(),
                    true,
                    null
            ));
            return result;
        } catch (Throwable error) {
            Instant endTime = Instant.now();
            collector.record(new TraceStepLog(
                    runtimeId,
                    moduleName,
                    inputSummary,
                    null,
                    startTime,
                    endTime,
                    Duration.between(startTime, endTime).toMillis(),
                    false,
                    error.getMessage()
            ));
            throw error;
        }
    }

    private String resolveRuntimeId(Object[] args) {
        for (Object arg : args) {
            if (arg instanceof RuntimeState state) {
                return state.getRuntimeId();
            }
        }
        String contextRuntimeId = TraceContextHolder.getRuntimeId();
        return contextRuntimeId == null ? "unknown" : contextRuntimeId;
    }

    private String summarizeArgs(Object[] args) {
        if (args == null || args.length == 0) {
            return "";
        }
        StringBuilder builder = new StringBuilder();
        for (Object arg : args) {
            if (arg instanceof RuntimeState state) {
                builder.append("runtimeId=").append(state.getRuntimeId());
            } else if (arg != null) {
                if (builder.length() > 0) {
                    builder.append(", ");
                }
                builder.append(arg.getClass().getSimpleName());
            }
        }
        return builder.toString();
    }

    private String summarizeResult(Object result) {
        if (result == null) {
            return "null";
        }
        return result.getClass().getSimpleName();
    }
}
