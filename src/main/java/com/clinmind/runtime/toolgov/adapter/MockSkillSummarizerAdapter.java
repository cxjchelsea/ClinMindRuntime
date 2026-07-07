package com.clinmind.runtime.toolgov.adapter;

import com.clinmind.runtime.toolgov.ToolInvocationRequest;
import com.clinmind.runtime.toolgov.ToolInvocationResult;
import com.clinmind.runtime.toolgov.ToolInvocationStatus;
import com.clinmind.runtime.toolgov.ToolRegistryEntry;
import com.clinmind.runtime.toolgov.ToolResultType;
import com.clinmind.runtime.toolgov.ToolType;
import com.clinmind.runtime.toolgov.ToolValidationStatus;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class MockSkillSummarizerAdapter implements ToolAdapter {

    @Override
    public boolean supports(ToolRegistryEntry entry) {
        return "mock_case_summary_skill".equals(entry.toolId()) || entry.toolType() == ToolType.SKILL_ADAPTER;
    }

    @Override
    public ToolInvocationResult invoke(ToolInvocationRequest request, ToolRegistryEntry entry) {
        Map<String, Object> result = Map.of(
                "summary_type", "structured_case_summary",
                "summary_points", List.of("redacted clinical context", "no patient-facing directive"),
                "safe_for_patient_output", false);
        return new ToolInvocationResult(
                request.invocationId(),
                entry.toolRegistryId(),
                entry.toolId(),
                entry.toolVersion(),
                ToolInvocationStatus.SUCCESS,
                ToolResultType.SKILL_RESULT,
                result,
                Map.of(),
                List.of(),
                null,
                0L,
                ToolValidationStatus.ACCEPTED,
                false,
                Map.of());
    }
}
