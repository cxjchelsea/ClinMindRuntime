package com.clinmind.runtime.console.access;

import org.springframework.stereotype.Component;

@Component
public class AccessPolicy {

    public void require(ActorContext context, ConsoleActionType actionType, ConsoleResourceType resourceType) {
        if (!isAllowed(context, actionType, resourceType)) {
            throw new AccessDeniedException(
                    actionType,
                    resourceType,
                    "Access denied for action "
                            + actionType.name()
                            + " on resource "
                            + resourceType.name());
        }
    }

    public boolean isAllowed(ActorContext context, ConsoleActionType actionType, ConsoleResourceType resourceType) {
        if (context.isSystemAdmin()) {
            return true;
        }
        for (DebugRole role : context.roles()) {
            if (RolePolicy.allows(role, actionType, resourceType)) {
                return true;
            }
        }
        return false;
    }
}
