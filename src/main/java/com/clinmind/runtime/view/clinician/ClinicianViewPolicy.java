package com.clinmind.runtime.view.clinician;

import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.DebugRole;
import com.clinmind.runtime.view.common.ViewProjectionException;
import org.springframework.stereotype.Component;

@Component
public class ClinicianViewPolicy {

    public void requireRead(ActorContext context) {
        if (context.hasRole(DebugRole.CLINICIAN) || context.isSystemAdmin()) {
            return;
        }
        throw new ViewProjectionException("CLINICIAN_CASE_FORBIDDEN", "Clinician case view is unavailable for actor roles: " + context.roles());
    }
}
