package com.clinmind.runtime.provider.api;

import com.clinmind.runtime.agent.api.AgentAccessDeniedException;
import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.DebugRole;
import org.springframework.stereotype.Component;

@Component
public class ProviderDebugAccessPolicy {

    public void requireRunAccess(ActorContext context) {
        if (context == null) {
            throw new AgentAccessDeniedException("actor context missing");
        }
        if (context.isSystemAdmin() || context.hasRole(DebugRole.EVALUATION_REVIEWER)) {
            return;
        }
        throw new AgentAccessDeniedException("provider debug run requires SYSTEM_ADMIN or EVALUATION_REVIEWER");
    }

    public void requireReadAccess(ActorContext context) {
        if (context == null) {
            throw new AgentAccessDeniedException("actor context missing");
        }
        if (context.isSystemAdmin()
                || context.hasRole(DebugRole.EVALUATION_REVIEWER)
                || context.hasRole(DebugRole.READ_ONLY_OBSERVER)) {
            return;
        }
        throw new AgentAccessDeniedException("provider debug read access denied");
    }
}
