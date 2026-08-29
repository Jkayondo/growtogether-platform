package africa.growtogether.platform.ecs;

import africa.growtogether.platform.common.security.GtPrincipal;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.jupiter.api.Test;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import static org.assertj.core.api.Assertions.assertThat;

class ConfigurationServiceAuditActorTest {

    @Test
    void gtPrincipalUsesCanonicalUsernameInsteadOfPrincipalRepresentation() {

        Set<String> permissions =
                IntStream.range(
                                0,
                                100
                        )
                        .mapToObj(
                                value ->
                                        "permission."
                                                + value
                                                + ".manage"
                        )
                        .collect(
                                Collectors.toSet()
                        );

        GtPrincipal principal =
                new GtPrincipal(
                        UUID.randomUUID(),
                        "admin",
                        UUID.randomUUID(),
                        Set.of(
                                "SCHOOL_ADMIN",
                                "INTEGRATION_ADMIN"
                        ),
                        permissions,
                        UUID.randomUUID()
                );

        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        principal,
                        "not-used",
                        Collections.emptyList()
                );

        assertThat(
                principal.toString().length()
        ).isGreaterThan(
                160
        );

        assertThat(
                ConfigurationService.auditActor(
                        authentication
                )
        ).isEqualTo(
                "admin"
        );
    }


    @Test
    void missingGtUsernameFallsBackToStableUserId() {

        UUID userId =
                UUID.randomUUID();

        GtPrincipal principal =
                new GtPrincipal(
                        userId,
                        "   ",
                        UUID.randomUUID(),
                        Set.of(),
                        Set.of(),
                        UUID.randomUUID()
                );

        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        principal,
                        "not-used",
                        Collections.emptyList()
                );

        assertThat(
                ConfigurationService.auditActor(
                        authentication
                )
        ).isEqualTo(
                userId.toString()
        );
    }


    @Test
    void oversizedUnexpectedAuthenticationNameIsNeverPersisted() {

        var authentication =
                UsernamePasswordAuthenticationToken.authenticated(
                        "x".repeat(500),
                        "not-used",
                        Collections.emptyList()
                );

        String actor =
                ConfigurationService.auditActor(
                        authentication
                );

        assertThat(actor)
                .isEqualTo(
                        "authenticated-user"
                );

        assertThat(actor.length())
                .isLessThanOrEqualTo(
                        160
                );
    }


    @Test
    void missingAuthenticationUsesSystemActor() {

        assertThat(
                ConfigurationService.auditActor(
                        null
                )
        ).isEqualTo(
                "system"
        );
    }
}
