package com.clinmind.runtime.toolgov.adapter;

import com.clinmind.runtime.toolgov.ToolInvocationRequest;
import com.clinmind.runtime.toolgov.ToolInvocationResult;
import com.clinmind.runtime.toolgov.ToolInvocationStatus;
import com.clinmind.runtime.toolgov.ToolRegistryEntry;
import com.clinmind.runtime.toolgov.ToolResultType;
import com.clinmind.runtime.toolgov.ToolValidationStatus;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class LocalClinicalCalculatorToolAdapter implements ToolAdapter {

    @Override
    public boolean supports(ToolRegistryEntry entry) {
        return "local_clinical_calculator".equals(entry.toolId());
    }

    @Override
    public ToolInvocationResult invoke(ToolInvocationRequest request, ToolRegistryEntry entry) {
        Number input = request.inputPayload().get("value") instanceof Number number ? number : 0;
        Map<String, Object> result = Map.of(
                "calculation_type", request.inputPayload().getOrDefault("calculation_type", "demo_score"),
                "score", Math.max(0, input.intValue()),
                "interpretation_boundary", "not_a_diagnosis");
        return new ToolInvocationResult(
                request.invocationId(),
                entry.toolRegistryId(),
                entry.toolId(),
                entry.toolVersion(),
                ToolInvocationStatus.SUCCESS,
                ToolResultType.TOOL_RESULT,
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
