package com.clinmind.runtime.evidence.runtime;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.EvidenceRetrievalResult;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "clinmind.python-provider.enabled=false")
class EvidenceRetrievalRuntimeConcurrencyTest {

    @Autowired
    private EvidenceRetrievalRuntime evidenceRetrievalRuntime;

    @Test
    void providerEnhancementIsRequestScopedUnderConcurrentRetrieve() throws Exception {
        int threadCount = 16;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        try {
            List<Callable<EvidenceRetrievalResult>> tasks = new ArrayList<>();
            for (int index = 0; index < threadCount; index++) {
                String runtimeId = "rt_concurrent_" + index;
                tasks.add(() -> evidenceRetrievalRuntime.retrieve(sampleRequest(runtimeId)));
            }
            List<Future<EvidenceRetrievalResult>> futures = executor.invokeAll(tasks);
            for (int index = 0; index < futures.size(); index++) {
                EvidenceRetrievalResult result = futures.get(index).get();
                String expectedRuntimeId = "rt_concurrent_" + index;
                assertThat(result.runtimeId()).isEqualTo(expectedRuntimeId);
                assertThat(result.providerEnhancement()).isNotNull();
                assertThat(result.providerEnhancement().trace().runtimeId()).isEqualTo(expectedRuntimeId);
            }
        } finally {
            executor.shutdownNow();
        }
    }

    private EvidenceRetrievalRequest sampleRequest(String runtimeId) {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("known_facts", List.of("胸闷"));
        return new EvidenceRetrievalRequest(
                "req_" + runtimeId,
                runtimeId,
                "chest_pain",
                summary,
                List.of("胸闷"),
                List.of(),
                List.of(),
                List.of(),
                "phase2-default",
                "0.2.0",
                EvidenceConstants.DEFAULT_RETRIEVAL_LIMIT,
                "test");
    }
}
