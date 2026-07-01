package com.clinmind.runtime.console.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class AccessPolicyTest {

    private AccessPolicy accessPolicy;

    @BeforeEach
    void setUp() {
        accessPolicy = new AccessPolicy();
    }

    @Test
    void systemAdminAllowsAllActions() {
        ActorContext admin = context(List.of(DebugRole.SYSTEM_ADMIN));

        assertThat(accessPolicy.isAllowed(admin, ConsoleActionType.REVIEW, ConsoleResourceType.CONSOLE_REVIEW))
                .isTrue();
        assertThat(accessPolicy.isAllowed(admin, ConsoleActionType.READ_AUDIT, ConsoleResourceType.CONSOLE_AUDIT))
                .isTrue();
    }

    @Test
    void candidateReviewerCanReviewButNotReadAudit() {
        ActorContext reviewer = context(List.of(DebugRole.CANDIDATE_REVIEWER));

        assertThat(accessPolicy.isAllowed(reviewer, ConsoleActionType.REVIEW, ConsoleResourceType.CONSOLE_REVIEW))
                .isTrue();
        assertThat(accessPolicy.isAllowed(reviewer, ConsoleActionType.READ_AUDIT, ConsoleResourceType.CONSOLE_AUDIT))
                .isFalse();
    }

    @Test
    void readOnlyObserverCanReadSummaryButNotReview() {
        ActorContext observer = context(List.of(DebugRole.READ_ONLY_OBSERVER));

        assertThat(accessPolicy.isAllowed(
                        observer, ConsoleActionType.READ_SUMMARY, ConsoleResourceType.CONSOLE_CANDIDATE))
                .isTrue();
        assertThat(accessPolicy.isAllowed(observer, ConsoleActionType.REVIEW, ConsoleResourceType.CONSOLE_REVIEW))
                .isFalse();
        assertThat(accessPolicy.isAllowed(
                        observer, ConsoleActionType.READ_DETAIL, ConsoleResourceType.CONSOLE_CANDIDATE))
                .isFalse();
    }

    @Test
    void auditReviewerCanReadAuditOnly() {
        ActorContext auditReviewer = context(List.of(DebugRole.AUDIT_REVIEWER));

        assertThat(accessPolicy.isAllowed(
                        auditReviewer, ConsoleActionType.READ_AUDIT, ConsoleResourceType.CONSOLE_AUDIT))
                .isTrue();
        assertThat(accessPolicy.isAllowed(
                        auditReviewer, ConsoleActionType.READ_SUMMARY, ConsoleResourceType.CONSOLE_CANDIDATE))
                .isFalse();
    }

    @Test
    void requireThrowsAccessDeniedWhenRoleInsufficient() {
        ActorContext observer = context(List.of(DebugRole.READ_ONLY_OBSERVER));

        assertThatThrownBy(() -> accessPolicy.require(
                        observer, ConsoleActionType.REVIEW, ConsoleResourceType.CONSOLE_REVIEW))
                .isInstanceOf(AccessDeniedException.class);
    }

    private static ActorContext context(List<DebugRole> roles) {
        return new ActorContext("actor_001", "tester", roles, "req_test", Instant.parse("2026-07-01T10:00:00Z"));
    }
}
