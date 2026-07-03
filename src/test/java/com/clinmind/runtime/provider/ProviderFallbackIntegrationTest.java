package com.clinmind.runtime.provider;

import static org.assertj.core.api.Assertions.assertThat;

import com.clinmind.runtime.audit.AuditActionType;
import com.clinmind.runtime.audit.AuditLogQuery;
import com.clinmind.runtime.audit.AuditLogService;
import com.clinmind.runtime.evidence.EvidenceConstants;
import com.clinmind.runtime.evidence.EvidenceRetrievalRequest;
import com.clinmind.runtime.evidence.runtime.EvidenceRetrievalRuntime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = "clinmind.python-provider.enabled=false")
class ProviderFallbackIntegrationTest {

    @Autowired
    private EvidenceRetrievalRuntime evidenceRetrievalRuntime;

    @Autowired
    private AuditLogService auditLogService;

    @Test
    void evidenceRetrievalFallsBackWhenPythonDisabled() {
        Map<String, Object> summary = new LinkedHashMap<>();
        summary.put("known_facts", List.of("胸闷"));
        EvidenceRetrievalRequest request = new EvidenceRetrievalRequest(
                "req_fallback",
                "rt_fallback",
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

        var result = evidenceRetrievalRuntime.retrieve(request);
        assertThat(result.warnings()).anyMatch(warning -> warning.contains("PROVIDER_RERANK_FALLBACK"));
        assertThat(auditLogService.query(new AuditLogQuery(
                        java.util.Optional.empty(),
                        java.util.Optional.of(AuditActionType.RUN_PYTHON_PROVIDER),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        java.util.Optional.empty(),
                        20))
                .stream()
                .anyMatch(record -> "rt_fallback".equals(record.metadata().get("runtime_id"))))
                .isTrue();
    }
}
