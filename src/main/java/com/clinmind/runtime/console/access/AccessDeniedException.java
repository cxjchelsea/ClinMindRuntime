package com.clinmind.runtime.console.access;

public class AccessDeniedException extends RuntimeException {

    private final ConsoleActionType actionType;
    private final ConsoleResourceType resourceType;

    public AccessDeniedException(
            ConsoleActionType actionType, ConsoleResourceType resourceType, String message) {
        super(message);
        this.actionType = actionType;
        this.resourceType = resourceType;
    }

    public ConsoleActionType getActionType() {
        return actionType;
    }

    public ConsoleResourceType getResourceType() {
        return resourceType;
    }
}
