package com.clinmind.runtime.view.clinician;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.clinmind.runtime.view.common.RoleSpecificViewSafetyPolicy;
import com.clinmind.runtime.view.common.RoleSpecificViewSanitizer;
import com.clinmind.runtime.view.common.ViewProjectionException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClinicianViewSanitizerTest {

    private final RoleSpecificViewSanitizer sanitizer =
            new RoleSpecificViewSanitizer(new RoleSpecificViewSafetyPolicy(), new ObjectMapper());

    @Test
    void stripsClinicianForbiddenMetadata() {
        Map<String, Object> sanitized = sanitizer.sanitizeClinicianMetadata(Map.of(
                "runtime_id", "runtime-demo-001",
                "api_key", "hidden",
                "raw_external_response", "hidden"));

        assertThat(sanitized).containsEntry("runtime_id", "runtime-demo-001");
        assertThat(sanitized).doesNotContainKeys("api_key", "raw_external_response");
    }

    @Test
    void rejectsClinicianDtoWithForbiddenField() {
        assertThatThrownBy(() -> sanitizer.validateClinicianViewDto(Map.of(
                "evidence_panel", "summary",
                "full_rationale", "hidden")))
                .isInstanceOf(ViewProjectionException.class)
                .hasMessageContaining("full_rationale");
    }
}
