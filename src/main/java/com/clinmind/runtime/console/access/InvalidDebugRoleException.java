package com.clinmind.runtime.console.access;

public class InvalidDebugRoleException extends RuntimeException {

    private final String invalidRole;

    public InvalidDebugRoleException(String invalidRole) {
        super("Invalid debug role: " + invalidRole);
        this.invalidRole = invalidRole;
    }

    public String getInvalidRole() {
        return invalidRole;
    }
}
