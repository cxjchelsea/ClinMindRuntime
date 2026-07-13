package com.clinmind.runtime.view.patient;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.view.common.RoleSpecificViewSafetyPolicy;
import com.clinmind.runtime.view.common.RoleSpecificViewSanitizer;
import com.clinmind.runtime.view.common.ViewProjectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PatientViewSanitizerTest {

    private final RoleSpecificViewSanitizer sanitizer =
            new RoleSpecificViewSanitizer(new RoleSpecificViewSafetyPolicy(), new ObjectMapper());

    @Test
    void stripsPatientForbiddenMetadata() {
        Map<String, Object> sanitized = sanitizer.sanitizePatientMetadata(Map.of(
                "runtime_id", "runtime-demo-001",
                "raw_evidence", "hidden",
                "model_prompt", "hidden"));

        assertThat(sanitized).containsEntry("runtime_id", "runtime-demo-001");
        assertThat(sanitized).doesNotContainKeys("raw_evidence", "model_prompt");
    }

    @Test
    void rejectsPatientDtoWithForbiddenField() {
        assertThatThrownBy(() -> sanitizer.validatePatientViewDto(Map.of(
                "safe_summary", "ok",
                "ddx_candidates", "hidden")))
                .isInstanceOf(ViewProjectionException.class)
                .hasMessageContaining("ddx_candidates");
    }
}
