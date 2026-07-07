package com.clinmind.runtime.toolgov.adapter;

import com.clinmind.runtime.toolgov.ToolInvocationRequest;
import com.clinmind.runtime.toolgov.ToolInvocationResult;
import com.clinmind.runtime.toolgov.ToolRegistryEntry;

public interface ToolAdapter {

    boolean supports(ToolRegistryEntry entry);

    ToolInvocationResult invoke(ToolInvocationRequest request, ToolRegistryEntry entry);
}
