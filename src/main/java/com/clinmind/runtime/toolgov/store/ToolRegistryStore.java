package com.clinmind.runtime.toolgov.store;

import com.clinmind.runtime.toolgov.ToolRegistryEntry;
import org.springframework.stereotype.Repository;

@Repository
public class ToolRegistryStore extends InMemoryToolGovernanceStore<ToolRegistryEntry> {
}
