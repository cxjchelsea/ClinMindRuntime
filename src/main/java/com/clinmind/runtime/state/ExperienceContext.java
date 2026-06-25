package com.clinmind.runtime.state;

import java.util.List;

public record ExperienceContext(
        List<ExperienceUnit> matchedExperienceUnits,
        List<String> experienceAlerts,
        String implementationMode
) {
    public ExperienceContext {
        matchedExperienceUnits = matchedExperienceUnits == null ? List.of() : List.copyOf(matchedExperienceUnits);
        experienceAlerts = experienceAlerts == null ? List.of() : List.copyOf(experienceAlerts);
        implementationMode = implementationMode == null ? "empty" : implementationMode;
    }

    public ExperienceContext() {
        this(List.of(), List.of(), "empty");
    }
}
