package com.clinmind.runtime.state;

public record SymptomItem(
        String name,
        String duration,
        String severity,
        String location,
        String trigger,
        String frequency,
        String relief
) {
}
