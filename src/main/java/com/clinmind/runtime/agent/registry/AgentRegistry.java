package com.clinmind.runtime.agent.registry;

import com.clinmind.runtime.agent.AgentMetadata;
import java.util.List;
import java.util.Optional;

public interface AgentRegistry {

    Optional<AgentMetadata> findById(String agentId);

    List<AgentMetadata> listAll();

    boolean isRegistered(String agentId);
}
