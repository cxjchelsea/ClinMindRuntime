package com.clinmind.runtime.toolgov;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.springframework.stereotype.Service;

@Service
public class ToolResultValidationService {

    public ToolValidationResult validate(ToolInvocationResult result, ToolRegistryEntry entry) {
        List<String> reasons = new ArrayList<>();
        if (result == null) {
            return ToolValidationResult.rejected(List.of("tool result missing"));
        }
        if (result.resultType() == null) {
            reasons.add("result_type missing");
        }
        scanMap(result.structuredResult(), reasons);
        scanMap(result.externalContext(), reasons);
        if (result.structuredResult().containsKey("patientOutput")
                || result.structuredResult().containsKey("patient_output")
                || result.externalContext().containsKey("patientOutput")
                || result.externalContext().containsKey("patient_output")) {
            reasons.add("PatientOutput field is forbidden");
        }
        if (entry != null && result.resultType() != ToolResultType.FALLBACK && result.resultType() != ToolResultType.NO_OP
                && !entry.requiresValidation()) {
            reasons.add("tool result requires validation");
        }
        return reasons.isEmpty() ? ToolValidationResult.accepted() : ToolValidationResult.rejected(reasons);
    }

    private void scanMap(Map<String, Object> values, List<String> reasons) {
        String flattened = String.valueOf(values).toLowerCase(Locale.ROOT);
        if (flattened.contains("final diagnosis") || flattened.contains("最终诊断")) {
            reasons.add("final diagnosis expression is forbidden");
        }
        if (flattened.contains("treatment instruction")
                || flattened.contains("prescribe ")
                || flattened.contains("用药")
                || flattened.contains("处方")) {
            reasons.add("treatment instruction expression is forbidden");
        }
        if (flattened.contains("secret") || flattened.contains("api_key") || flattened.contains("private_key")) {
            reasons.add("secret-like content is forbidden");
        }
        if (flattened.contains("<script") || flattened.contains("powershell") || flattened.contains("cmd.exe")) {
            reasons.add("executable script content is forbidden");
        }
        if (flattened.contains("raw_external_response") || flattened.length() > 4000) {
            reasons.add("raw external response is forbidden");
        }
    }
}
