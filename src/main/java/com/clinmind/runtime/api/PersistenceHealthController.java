package com.clinmind.runtime.api;

import com.clinmind.runtime.config.ClinmindPersistenceProperties;
import java.util.LinkedHashMap;
import java.util.Map;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug/persistence")
public class PersistenceHealthController {

    private final ClinmindPersistenceProperties persistenceProperties;

    public PersistenceHealthController(ClinmindPersistenceProperties persistenceProperties) {
        this.persistenceProperties = persistenceProperties;
    }

    @GetMapping("/health")
    public ApiResponse<Map<String, Object>> health() {
        Map<String, Object> data = new LinkedHashMap<>();
        String mode = persistenceProperties.getMode();
        data.put("mode", mode);
        if (persistenceProperties.isPostgres()) {
            data.put("database", "PostgreSQL");
            data.put("schema_version", "5.0.0");
            data.put("runtime_store", "jdbc");
            data.put("evaluation_store", "jdbc");
            data.put("candidate_store", "jdbc");
            data.put("review_store", "jdbc");
            data.put("audit_log_store", "jdbc");
        } else {
            data.put("database", "none");
            data.put("schema_version", "n/a");
            data.put("runtime_store", "in-memory");
            data.put("evaluation_store", "in-memory");
            data.put("candidate_store", "in-memory");
            data.put("review_store", "in-memory");
            data.put("audit_log_store", "in-memory");
        }
        return ApiResponse.ok(data);
    }
}
