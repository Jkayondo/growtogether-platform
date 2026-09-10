package africa.growtogether.platform.eiam.permission;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.eiam.role.Role;
import africa.growtogether.platform.eiam.role.RoleRepository;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class RolePermissionReplacementPostgresIntegrationTest {
    @Container
    static final PostgreSQLContainer<?> POSTGRES =
        new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.data.redis.repositories.enabled", () -> "false");
    }

    @Autowired PermissionService service;
    @Autowired PermissionRepository permissions;
    @Autowired RoleRepository roles;
    @Autowired RolePermissionRepository links;
    @Autowired PlatformTransactionManager transactionManager;
    @Autowired JdbcTemplate jdbc;

    private TransactionTemplate transaction;
    private UUID tenant;
    private UUID roleId;
    private UUID first;
    private UUID second;
    private UUID otherRoleId;

    @BeforeEach
    void seedCommittedAssignments() {
        transaction = new TransactionTemplate(transactionManager);
        tenant = jdbc.queryForObject(
            "SELECT id FROM eiam_tenant WHERE code = 'GT-SCHOOL'", UUID.class);
        RequestContextHolder.set(new RequestContext("role-replacement-test", tenant.toString()));

        transaction.executeWithoutResult(status -> {
            String suffix = UUID.randomUUID().toString();
            Role role = roles.saveAndFlush(
                new Role("TEST_REPLACE_" + suffix, "Replacement test " + suffix, null, false));
            Role other = roles.saveAndFlush(
                new Role("TEST_OTHER_" + suffix, "Untouched role " + suffix, null, false));
            Permission a = permissions.saveAndFlush(
                new Permission("test.first." + suffix, "First", "TEST", null, false));
            Permission b = permissions.saveAndFlush(
                new Permission("test.second." + suffix, "Second", "TEST", null, false));
            roleId = role.getId();
            otherRoleId = other.getId();
            first = a.getId();
            second = b.getId();
            links.saveAndFlush(new RolePermission(roleId, first));
            links.saveAndFlush(new RolePermission(otherRoleId, first));
        });
    }

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    private void replace(Set<UUID> ids) {
        transaction.executeWithoutResult(status ->
            service.replaceRolePermissions(roleId, new ReplaceRolePermissionsCommand(ids)));
    }

    private void assertStored(UUID targetRole, UUID... expected) {
        transaction.executeWithoutResult(status ->
            assertThat(links.findAllByTenantIdAndRoleId(tenant, targetRole))
                .extracting(RolePermission::getPermissionId)
                .containsExactlyInAnyOrder(expected));
    }

    @Test
    void replacesOverlappingSetsAndSupportsRepeatRemovalAndClear() {
        replace(Set.of(first, second));
        assertStored(roleId, first, second);

        replace(Set.of(first, second));
        assertStored(roleId, first, second);

        replace(Set.of(second));
        assertStored(roleId, second);

        replace(Set.of());
        assertStored(roleId);
        assertStored(otherRoleId, first);
    }

    @Test
    void rollbackRestoresOriginalLinksAfterReplacementWasFlushed() {
        assertThatThrownBy(() ->
            transaction.executeWithoutResult(status -> {
                service.replaceRolePermissions(
                    roleId, new ReplaceRolePermissionsCommand(Set.of(first, second)));
                throw new IllegalStateException("Deliberate rollback");
            }))
            .isInstanceOf(IllegalStateException.class)
            .hasMessage("Deliberate rollback");

        assertStored(roleId, first);
        assertStored(otherRoleId, first);
    }
}
