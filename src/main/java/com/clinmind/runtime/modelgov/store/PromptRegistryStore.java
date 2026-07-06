package com.clinmind.runtime.modelgov.store;

import com.clinmind.runtime.modelgov.PromptRegistryEntry;
import org.springframework.stereotype.Component;

@Component
public class PromptRegistryStore extends InMemoryGovernanceStore<PromptRegistryEntry> {
}
