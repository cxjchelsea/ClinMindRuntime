package com.clinmind.runtime.boundary;

import com.clinmind.runtime.knowledge.CapabilityProfile;
import com.clinmind.runtime.knowledge.StaticRuleProvider;
import org.springframework.stereotype.Component;

@Component
public class CapabilityProfileProvider {

    private final StaticRuleProvider staticRuleProvider;

    public CapabilityProfileProvider(StaticRuleProvider staticRuleProvider) {
        this.staticRuleProvider = staticRuleProvider;
    }

    public CapabilityProfile loadProfile(String symptomGroup) {
        return staticRuleProvider.loadCapabilityProfile(symptomGroup);
    }
}
