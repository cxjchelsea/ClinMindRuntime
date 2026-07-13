package com.clinmind.runtime.view.patient;

import com.clinmind.runtime.console.access.ActorContext;
import com.clinmind.runtime.console.access.DebugRole;
import com.clinmind.runtime.view.common.ViewProjectionException;
import org.springframework.stereotype.Component;

@Component
public class PatientViewPolicy {

    public void requireRead(ActorContext context) {
        if (context.hasRole(DebugRole.PATIENT) || context.isSystemAdmin()) {
            return;
        }
        throw new ViewProjectionException("PATIENT_VIEW_FORBIDDEN", "Patient view is unavailable for actor roles: " + context.roles());
    }
}
