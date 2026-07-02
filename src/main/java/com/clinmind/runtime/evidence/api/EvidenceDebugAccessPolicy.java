package com.clinmind.runtime.evidence.api;

import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.DebugRole;
import org.springframework.stereotype.Component;

@Component
public class EvidenceDebugAccessPolicy {

    public void requireRunAccess(ActorContext context) {
        if (context == null) {
            throw new EvidenceAccessDeniedException("actor context missing");
        }
        if (context.isSystemAdmin() || context.hasRole(DebugRole.EVALUATION_REVIEWER)) {
            return;
        }
        throw new EvidenceAccessDeniedException(
                "evidence debug run requires SYSTEM_ADMIN or EVALUATION_REVIEWER");
    }

    public void requireReadAccess(ActorContext context) {
        if (context == null) {
            throw new EvidenceAccessDeniedException("actor context missing");
        }
        if (context.isSystemAdmin()
                || context.hasRole(DebugRole.EVALUATION_REVIEWER)
                || context.hasRole(DebugRole.READ_ONLY_OBSERVER)) {
            return;
        }
        throw new EvidenceAccessDeniedException("evidence debug read access denied");
    }
}
