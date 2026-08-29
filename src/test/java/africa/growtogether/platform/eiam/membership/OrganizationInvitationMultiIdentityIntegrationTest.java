package africa.growtogether.platform.eiam.membership;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
@Transactional
class OrganizationInvitationMultiIdentityIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>(
            "postgres:17-alpine"
        );

    @DynamicPropertySource
    static void properties(
            DynamicPropertyRegistry registry
    ) {
        registry.add(
            "spring.datasource.url",
            POSTGRES::getJdbcUrl
        );

        registry.add(
            "spring.datasource.username",
            POSTGRES::getUsername
        );

        registry.add(
            "spring.datasource.password",
            POSTGRES::getPassword
        );

        registry.add(
            "spring.flyway.enabled",
            () -> "true"
        );

        registry.add(
            "spring.data.redis.repositories.enabled",
            () -> "false"
        );
    }

    @Autowired
    OrganizationInvitationRepository repository;

    @Autowired
    EntityManager entityManager;

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void persistsAndFindsPendingPhoneInvitation()
            throws Exception {

        UUID tenantId =
            UUID.randomUUID();

        String phone =
            "+256703456789";

        RequestContextHolder.set(
            new RequestContext(
                "test",
                tenantId.toString()
            )
        );

        OrganizationInvitation invitation =
            new OrganizationInvitation(
                null,
                phone,
                repeatedHash('a'),
                Instant.now().plusSeconds(3600),
                null
            );

        repository.saveAndFlush(
            invitation
        );

        UUID invitationId =
            invitation.getId();

        entityManager.clear();

        OrganizationInvitation stored =
            repository
                .findFirstByTenantIdAndPhoneNumberAndInvitationStatus(
                    tenantId,
                    phone,
                    InvitationStatus.PENDING
                )
                .orElseThrow();

        assertThat(stored.getId())
            .isEqualTo(invitationId);

        assertThat(stored.getTenantId())
            .isEqualTo(tenantId);

        assertThat(stored.getEmail())
            .isNull();

        assertThat(stored.getPhoneNumber())
            .isEqualTo(phone);

        assertThat(stored.isPhoneTarget())
            .isTrue();

        assertThat(stored.isEmailTarget())
            .isFalse();

        assertThat(stored.getInvitationStatus())
            .isEqualTo(InvitationStatus.PENDING);
    }

    @Test
    void preventsDuplicatePendingPhoneInvitationWithinTenant() {

        UUID tenantId =
            UUID.randomUUID();

        String phone =
            "+256704567890";

        RequestContextHolder.set(
            new RequestContext(
                "test",
                tenantId.toString()
            )
        );

        OrganizationInvitation first =
            new OrganizationInvitation(
                null,
                phone,
                repeatedHash('b'),
                Instant.now().plusSeconds(3600),
                null
            );

        repository.saveAndFlush(
            first
        );

        OrganizationInvitation duplicate =
            new OrganizationInvitation(
                null,
                phone,
                repeatedHash('c'),
                Instant.now().plusSeconds(3600),
                null
            );

        assertThatThrownBy(
            () -> repository.saveAndFlush(
                duplicate
            )
        ).isInstanceOf(
            DataIntegrityViolationException.class
        );
    }

    private static String repeatedHash(
            char value
    ) {
        return String.valueOf(value)
            .repeat(64);
    }
}
