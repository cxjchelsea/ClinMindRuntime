package com.clinmind.runtime.service;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeTrace;

public record RuntimeExecutionResult(RuntimeState state, RuntimeTrace trace) {
}
