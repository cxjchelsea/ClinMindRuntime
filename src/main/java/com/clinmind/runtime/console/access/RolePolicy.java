package com.clinmind.runtime.console.access;

final class RolePolicy {

    private RolePolicy() {
    }

    static boolean allows(DebugRole role, ConsoleActionType actionType, ConsoleResourceType resourceType) {
        return switch (resourceType) {
            case CONSOLE_RUNTIME -> allowsRuntime(role, actionType);
            case CONSOLE_EVALUATION -> allowsEvaluation(role, actionType);
            case CONSOLE_CANDIDATE -> allowsCandidate(role, actionType);
            case CONSOLE_REVIEW -> allowsReview(role, actionType);
            case CONSOLE_AUDIT -> allowsAudit(role, actionType);
            case CONSOLE_SYSTEM -> allowsSystem(role, actionType);
        };
    }

    private static boolean allowsRuntime(DebugRole role, ConsoleActionType actionType) {
        return switch (role) {
            case SYSTEM_ADMIN -> true;
            case EVALUATION_REVIEWER -> isSummaryOrList(actionType);
            case READ_ONLY_OBSERVER -> isSummaryOrList(actionType);
            default -> false;
        };
    }

    private static boolean allowsEvaluation(DebugRole role, ConsoleActionType actionType) {
        return switch (role) {
            case SYSTEM_ADMIN -> true;
            case EVALUATION_REVIEWER -> isSummaryOrList(actionType) || actionType == ConsoleActionType.READ_DETAIL;
            case CANDIDATE_REVIEWER, READ_ONLY_OBSERVER -> isSummaryOrList(actionType);
            default -> false;
        };
    }

    private static boolean allowsCandidate(DebugRole role, ConsoleActionType actionType) {
        return switch (role) {
            case SYSTEM_ADMIN -> true;
            case CANDIDATE_REVIEWER -> true;
            case READ_ONLY_OBSERVER -> isSummaryOrList(actionType);
            default -> false;
        };
    }

    private static boolean allowsReview(DebugRole role, ConsoleActionType actionType) {
        return switch (role) {
            case SYSTEM_ADMIN -> true;
            case CANDIDATE_REVIEWER -> actionType == ConsoleActionType.REVIEW
                    || actionType == ConsoleActionType.LIST
                    || actionType == ConsoleActionType.READ_SUMMARY
                    || actionType == ConsoleActionType.READ_DETAIL;
            default -> false;
        };
    }

    private static boolean allowsAudit(DebugRole role, ConsoleActionType actionType) {
        return switch (role) {
            case SYSTEM_ADMIN, AUDIT_REVIEWER -> actionType == ConsoleActionType.READ_AUDIT
                    || actionType == ConsoleActionType.READ_DETAIL
                    || actionType == ConsoleActionType.LIST
                    || actionType == ConsoleActionType.READ_SUMMARY;
            default -> false;
        };
    }

    private static boolean allowsSystem(DebugRole role, ConsoleActionType actionType) {
        return actionType == ConsoleActionType.READ_HEALTH;
    }

    private static boolean isSummaryOrList(ConsoleActionType actionType) {
        return actionType == ConsoleActionType.READ_SUMMARY || actionType == ConsoleActionType.LIST;
    }
}
