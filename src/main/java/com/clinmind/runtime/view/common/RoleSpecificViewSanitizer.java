package com.clinmind.runtime.view.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.springframework.stereotype.Component;

@Component
public class RoleSpecificViewSanitizer {

    private final RoleSpecificViewSafetyPolicy safetyPolicy;
    private final ObjectMapper objectMapper;

    public RoleSpecificViewSanitizer(RoleSpecificViewSafetyPolicy safetyPolicy, ObjectMapper objectMapper) {
        this.safetyPolicy = safetyPolicy;
        this.objectMapper = objectMapper;
    }

    public Map<String, Object> sanitizePatientMetadata(Map<String, Object> metadata) {
        return sanitize(metadata, true);
    }

    public Map<String, Object> sanitizeClinicianMetadata(Map<String, Object> metadata) {
        return sanitize(metadata, false);
    }

    public void validatePatientViewDto(Object dto) {
        validateDto(dto, true);
    }

    public void validateClinicianViewDto(Object dto) {
        validateDto(dto, false);
    }

    private Map<String, Object> sanitize(Map<String, Object> metadata, boolean patient) {
        if (metadata == null || metadata.isEmpty()) {
            return Map.of();
        }
        java.util.Map<String, Object> sanitized = new java.util.LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : metadata.entrySet()) {
            String key = entry.getKey();
            boolean forbidden = patient
                    ? safetyPolicy.isPatientForbidden(key)
                    : safetyPolicy.isClinicianForbidden(key);
            if (!forbidden) {
                sanitized.put(key, entry.getValue());
            }
        }
        return Map.copyOf(sanitized);
    }

    private void validateDto(Object dto, boolean patient) {
        Map<String, Object> tree = objectMapper.convertValue(dto, Map.class);
        java.util.List<String> violations = new java.util.ArrayList<>();
        collectViolations(tree, patient, violations);
        if (!violations.isEmpty()) {
            throw new ViewProjectionException(
                    "VIEW_PROJECTION_SANITIZATION_FAILED",
                    "Role-specific view contains forbidden fields: " + violations);
        }
    }

    @SuppressWarnings("unchecked")
    private void collectViolations(Object value, boolean patient, java.util.List<String> violations) {
        if (value instanceof Map<?, ?> map) {
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                String key = String.valueOf(entry.getKey());
                boolean forbidden = patient
                        ? safetyPolicy.isPatientForbidden(key)
                        : safetyPolicy.isClinicianForbidden(key);
                if (forbidden) {
                    violations.add(key);
                }
                collectViolations(entry.getValue(), patient, violations);
            }
        } else if (value instanceof Iterable<?> iterable) {
            for (Object item : iterable) {
                collectViolations(item, patient, violations);
            }
        }
    }
}
