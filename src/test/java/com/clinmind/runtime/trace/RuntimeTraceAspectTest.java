package com.clinmind.runtime.trace;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.state.RuntimeState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class RuntimeTraceAspectTest {

    @Autowired
    private SampleTracedService sampleTracedService;

    @Autowired
    private RuntimeTraceCollector collector;

    @BeforeEach
    void setUp() {
        collector.clear();
    }

    @Test
    void recordsSuccessfulTraceStep() {
        RuntimeState state = RuntimeState.createDefault("s_001");

        String result = sampleTracedService.process(state);

        assertThat(result).isEqualTo("processed");
        assertThat(collector.getSteps()).hasSize(1);
        TraceStepLog step = collector.getSteps().get(0);
        assertThat(step.moduleName()).isEqualTo("SampleModule");
        assertThat(step.runtimeId()).isEqualTo(state.getRuntimeId());
        assertThat(step.success()).isTrue();
        assertThat(step.durationMs()).isGreaterThanOrEqualTo(0);
        assertThat(step.errorMessage()).isNull();
    }

    @Test
    void recordsFailedTraceStep() {
        RuntimeState state = RuntimeState.createDefault("s_001");

        assertThatThrownBy(() -> sampleTracedService.fail(state))
                .isInstanceOf(IllegalStateException.class);

        assertThat(collector.getSteps()).hasSize(1);
        TraceStepLog step = collector.getSteps().get(0);
        assertThat(step.moduleName()).isEqualTo("FailingModule");
        assertThat(step.success()).isFalse();
        assertThat(step.errorMessage()).isEqualTo("module failed");
    }
}
