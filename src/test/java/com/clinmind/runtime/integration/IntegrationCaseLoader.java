package com.clinmind.runtime.integration;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.springframework.core.io.ClassPathResource;
import org.yaml.snakeyaml.Yaml;

public final class IntegrationCaseLoader {

    private IntegrationCaseLoader() {
    }

    @SuppressWarnings("unchecked")
    public static List<IntegrationCase> loadCases(String resourcePath) {
        try {
            ClassPathResource resource = new ClassPathResource(resourcePath);
            try (InputStreamReader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
                Object loaded = new Yaml().load(reader);
                if (!(loaded instanceof Map<?, ?> root)) {
                    return List.of();
                }
                List<Map<String, Object>> cases = (List<Map<String, Object>>) root.get("cases");
                if (cases == null) {
                    return List.of();
                }
                List<IntegrationCase> result = new ArrayList<>();
                for (Map<String, Object> item : cases) {
                    result.add(parseCase(item));
                }
                return Collections.unmodifiableList(result);
            }
        } catch (Exception error) {
            throw new IllegalStateException("Failed to load integration cases: " + resourcePath, error);
        }
    }

    @SuppressWarnings("unchecked")
    private static IntegrationCase parseCase(Map<String, Object> item) {
        Map<String, Object> input = mapValue(item.get("input"));
        Map<String, Object> expected = mapValue(item.get("expected"));
        List<Map<String, Object>> continueSteps = listMapValue(item.get("continue"));

        return new IntegrationCase(
                stringValue(item.get("case_id")),
                stringValue(item.get("title")),
                stringValue(item.get("mode")),
                stringValue(input.get("text")),
                mapValue(item.get("basic_info")),
                expected,
                continueSteps.stream().map(IntegrationCaseLoader::parseContinueStep).toList());
    }

    @SuppressWarnings("unchecked")
    private static IntegrationContinueStep parseContinueStep(Map<String, Object> step) {
        Map<String, Object> input = mapValue(step.get("input"));
        return new IntegrationContinueStep(
                stringValue(input.get("text")),
                mapValue(step.get("expected")));
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> mapValue(Object value) {
        if (value instanceof Map<?, ?> map) {
            return (Map<String, Object>) map;
        }
        return Map.of();
    }

    @SuppressWarnings("unchecked")
    private static List<Map<String, Object>> listMapValue(Object value) {
        if (!(value instanceof List<?> list)) {
            return List.of();
        }
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object item : list) {
            if (item instanceof Map<?, ?> map) {
                result.add((Map<String, Object>) map);
            }
        }
        return result;
    }

    private static String stringValue(Object value) {
        return value == null ? null : String.valueOf(value);
    }
}
