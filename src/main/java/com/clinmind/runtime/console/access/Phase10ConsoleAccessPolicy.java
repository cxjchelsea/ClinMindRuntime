package com.clinmind.runtime.console.access;

import org.springframework.stereotype.Component;

@Component
public class Phase10ConsoleAccessPolicy {

    public void requireRead(ActorContext context) {
        if (context.roles().contains(DebugRole.SYSTEM_ADMIN)
                || context.roles().contains(DebugRole.EVALUATION_REVIEWER)
                || context.roles().contains(DebugRole.READ_ONLY_OBSERVER)) {
            return;
        }
        throw new AccessDeniedException(
                ConsoleActionType.READ_SUMMARY,
                ConsoleResourceType.CONSOLE_SYSTEM,
                "Phase10 console is unavailable for actor roles: " + context.roles());
    }
}
