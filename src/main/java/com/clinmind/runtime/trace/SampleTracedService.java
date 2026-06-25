package com.clinmind.runtime.trace;

import com.clinmind.runtime.state.RuntimeState;
import org.springframework.stereotype.Service;

@Service
public class SampleTracedService {

    @TraceStep("SampleModule")
    public String process(RuntimeState state) {
        return "processed";
    }

    @TraceStep("FailingModule")
    public String fail(RuntimeState state) {
        throw new IllegalStateException("module failed");
    }
}
