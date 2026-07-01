package com.clinmind.runtime.console.access;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

class ActorContextResolverTest {

    private final ActorContextResolver resolver = new ActorContextResolver();

    @Test
    void resolvesActorAndRolesFromHeaders() {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.addHeader(ActorContextResolver.DEBUG_ACTOR_HEADER, "alice");
        request.addHeader(ActorContextResolver.DEBUG_ROLES_HEADER, "CANDIDATE_REVIEWER,AUDIT_REVIEWER");
        request.addHeader(ActorContextResolver.REQUEST_ID_HEADER, "req_001");

        ActorContext context = resolver.resolve(request);

        assertThat(context.actorName()).isEqualTo("alice");
        assertThat(context.roles()).containsExactly(
                DebugRole.CANDIDATE_REVIEWER, DebugRole.AUDIT_REVIEWER);
        assertThat(context.requestId()).isEqualTo("req_001");
    }

    @Test
    void defaultsToSystemDebugAndReadOnlyObserver() {
        ActorContext context = resolver.resolve(new MockHttpServletRequest());

        assertThat(context.actorName()).isEqualTo("system-debug");
        assertThat(context.roles()).containsExactly(DebugRole.READ_ONLY_OBSERVER);
        assertThat(context.requestId()).isNotBlank();
    }

    @Test
    void rejectsInvalidRoleToken() {
        assertThatThrownBy(() -> resolver.parseRoles("CANDIDATE_REVIEWER,NOT_A_ROLE"))
                .isInstanceOf(InvalidDebugRoleException.class)
                .hasMessageContaining("NOT_A_ROLE");
    }
}
