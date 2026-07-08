package com.clinmind.runtime.console.access;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ConsoleAccessPolicyTest {

    private final Phase10ConsoleAccessPolicy policy = new Phase10ConsoleAccessPolicy();

    @Test
    void phase10ReadRolesCanAccessConsole() {
        assertThatCode(() -> policy.requireRead(context(DebugRole.SYSTEM_ADMIN))).doesNotThrowAnyException();
        assertThatCode(() -> policy.requireRead(context(DebugRole.EVALUATION_REVIEWER))).doesNotThrowAnyException();
        assertThatCode(() -> policy.requireRead(context(DebugRole.READ_ONLY_OBSERVER))).doesNotThrowAnyException();
    }

    @Test
    void patientCannotAccessConsole() {
        assertThatThrownBy(() -> policy.requireRead(context(DebugRole.PATIENT)))
                .isInstanceOf(AccessDeniedException.class);
    }

    private ActorContext context(DebugRole role) {
        return new ActorContext("actor_phase10", "actor_phase10", List.of(role), "req_phase10", Instant.now());
    }
}
