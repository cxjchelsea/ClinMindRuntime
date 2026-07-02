package com.clinmind.runtime.agent;

public interface AgentProposal {

    String proposalId();

    String runtimeId();

    String agentId();

    String agentVersion();

    String proposalType();
}
