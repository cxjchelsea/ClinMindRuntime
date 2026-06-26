package com.clinmind.runtime.trace;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Component;

@Component
public class RuntimeTraceCollector {

    private final List<TraceStepLog> steps = new CopyOnWriteArrayList<>();

    public void record(TraceStepLog stepLog) {
        steps.add(stepLog);
    }

    public List<TraceStepLog> getSteps() {
        return Collections.unmodifiableList(new ArrayList<>(steps));
    }

    public List<TraceStepLog> getStepsByRuntimeId(String runtimeId) {
        return steps.stream()
                .filter(step -> runtimeId != null && runtimeId.equals(step.runtimeId()))
                .toList();
    }

    public List<TraceStepLog> drainStepsForRuntime(String runtimeId) {
        List<TraceStepLog> matched = steps.stream()
                .filter(step -> runtimeId != null && runtimeId.equals(step.runtimeId()))
                .toList();
        steps.removeIf(step -> runtimeId != null && runtimeId.equals(step.runtimeId()));
        return matched;
    }

    public void clear() {
        steps.clear();
    }
}
