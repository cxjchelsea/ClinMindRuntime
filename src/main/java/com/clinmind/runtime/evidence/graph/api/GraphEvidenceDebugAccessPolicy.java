package com.clinmind.runtime.evidence.graph.api;

import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.DebugRole;
import org.springframework.stereotype.Component;

@Component
public class GraphEvidenceDebugAccessPolicy {

    public void requireRunAccess(ActorContext context) {
        if (context == null) {
            throw new GraphEvidenceAccessDeniedException("actor context missing");
        }
        if (context.isSystemAdmin() || context.hasRole(DebugRole.EVALUATION_REVIEWER)) {
            return;
        }
        throw new GraphEvidenceAccessDeniedException(
                "graph evidence debug run requires SYSTEM_ADMIN or EVALUATION_REVIEWER");
    }

    public void requireReadAccess(ActorContext context) {
        if (context == null) {
            throw new GraphEvidenceAccessDeniedException("actor context missing");
        }
        if (context.isSystemAdmin()
                || context.hasRole(DebugRole.EVALUATION_REVIEWER)
                || context.hasRole(DebugRole.READ_ONLY_OBSERVER)) {
            return;
        }
        throw new GraphEvidenceAccessDeniedException("graph evidence debug read access denied");
    }
}
