package com.clinmind.runtime.modelgov.store;

import com.clinmind.runtime.modelgov.ModelRegistryEntry;
import org.springframework.stereotype.Component;

@Component
public class ModelRegistryStore extends InMemoryGovernanceStore<ModelRegistryEntry> {
}
