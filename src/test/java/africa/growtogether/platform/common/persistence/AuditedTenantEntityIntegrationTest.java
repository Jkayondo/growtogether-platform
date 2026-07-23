package africa.growtogether.platform.common.persistence;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers(disabledWithoutDocker = true)
@SpringBootTest
@Transactional
class AuditedTenantEntityIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:17-alpine")
            .withDatabaseName("growtogether_test")
            .withUsername("growtogether")
            .withPassword("growtogether");

    @DynamicPropertySource
    static void databaseProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
    }

    @Autowired
    private PlatformTenantRecordRepository repository;

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void assignsTenantAndAuditFieldsFromActiveContext() {
        UUID tenantId = UUID.randomUUID();

        RequestContextHolder.set(
            new RequestContext("test-correlation", tenantId.toString())
        );

        PlatformTenantRecord saved =
            repository.saveAndFlush(
                new PlatformTenantRecord("locale", "en-UG")
            );

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getTenantId()).isEqualTo(tenantId);
        assertThat(saved.getCreatedAt()).isNotNull();
        assertThat(saved.getUpdatedAt()).isNotNull();
        assertThat(saved.getCreatedBy())
            .isEqualTo(RequestContextAuditorAware.SYSTEM_AUDITOR);
        assertThat(saved.getUpdatedBy())
            .isEqualTo(RequestContextAuditorAware.SYSTEM_AUDITOR);
        assertThat(saved.getStatus()).isEqualTo(EntityStatus.ACTIVE);
        assertThat(saved.getVersion()).isZero();
    }

    @Test
    void incrementsVersionAndUpdatesAuditTimestamp() {
        UUID tenantId = UUID.randomUUID();

        RequestContextHolder.set(
            new RequestContext("test-correlation", tenantId.toString())
        );

        PlatformTenantRecord saved =
            repository.saveAndFlush(
                new PlatformTenantRecord(
                    "timezone",
                    "Africa/Kampala"
                )
            );

        var initialUpdatedAt = saved.getUpdatedAt();

        saved.setRecordValue("UTC");

        PlatformTenantRecord updated =
            repository.saveAndFlush(saved);

        assertThat(updated.getVersion()).isEqualTo(1L);
        assertThat(updated.getUpdatedAt())
            .isAfterOrEqualTo(initialUpdatedAt);
    }

    @Test
    void rejectsPersistenceWithoutTenantContext() {
        assertThatThrownBy(
            () -> repository.saveAndFlush(
                new PlatformTenantRecord("currency", "UGX")
            )
        )
            .isInstanceOf(TenantScopeViolationException.class)
            .hasMessage(
                "A tenant context is required for tenant-scoped persistence."
            );
    }

    @Test
    void rejectsEntityBelongingToAnotherTenant() {
        UUID activeTenant = UUID.randomUUID();
        UUID otherTenant = UUID.randomUUID();

        RequestContextHolder.set(
            new RequestContext(
                "test-correlation",
                activeTenant.toString()
            )
        );

        PlatformTenantRecord record =
            new PlatformTenantRecord(
                "language",
                "English"
            );

        record.setTenantId(otherTenant);

        assertThatThrownBy(
            () -> repository.saveAndFlush(record)
        )
            .isInstanceOf(TenantScopeViolationException.class)
            .hasMessage(
                "Entity tenant does not match the active tenant context."
            );
    }
}
