package com.clinmind.runtime.modelgov.store;

import com.clinmind.runtime.modelgov.ModelExperimentRecord;
import org.springframework.stereotype.Component;

@Component
public class ModelExperimentStore extends InMemoryGovernanceStore<ModelExperimentRecord> {
}
