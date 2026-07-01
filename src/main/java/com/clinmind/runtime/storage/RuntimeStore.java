package com.clinmind.runtime.storage;

import com.clinmind.runtime.state.RuntimeState;
import com.clinmind.runtime.state.RuntimeStatus;
import com.clinmind.runtime.state.RuntimeTrace;
import java.util.List;

public interface RuntimeStore {

    RuntimeState create(RuntimeState state);

    RuntimeState get(String runtimeId);

    RuntimeState update(RuntimeState state);

    boolean exists(String runtimeId);

    RuntimeTrace addTrace(RuntimeTrace trace);

    List<RuntimeTrace> getTraces(String runtimeId);

    List<RuntimeState> list(String sessionId, RuntimeStatus status, int limit);
}
