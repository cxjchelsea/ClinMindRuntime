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
public class MockGuidelineLookupToolAdapter implements ToolAdapter {

    @Override
    public boolean supports(ToolRegistryEntry entry) {
        return "mock_guideline_lookup".equals(entry.toolId()) || entry.toolType() == ToolType.MOCK_EXTERNAL;
    }

    @Override
    public ToolInvocationResult invoke(ToolInvocationRequest request, ToolRegistryEntry entry) {
        Map<String, Object> context = Map.of(
                "topic", request.inputPayload().getOrDefault("topic", "general_guideline_metadata"),
                "source_type", "mock_guideline_metadata",
                "intended_use", "clinician_context_only");
        return new ToolInvocationResult(
                request.invocationId(),
                entry.toolRegistryId(),
                entry.toolId(),
                entry.toolVersion(),
                ToolInvocationStatus.SUCCESS,
                ToolResultType.EXTERNAL_CONTEXT,
                context,
                context,
                List.of(),
                null,
                0L,
                ToolValidationStatus.ACCEPTED,
                false,
                Map.of());
    }
}
