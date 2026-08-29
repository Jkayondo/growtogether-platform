package africa.growtogether.platform.connect;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class ConnectPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_connect_test"
                    )
                    .withUsername(
                            "growtogether"
                    )
                    .withPassword(
                            "growtogether"
                    );

    @DynamicPropertySource
    static void databaseProperties(
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
    private ConnectSpaceRepository spaces;

    @Autowired
    private ConnectSpaceMemberRepository members;

    @Autowired
    private ConnectMessageRepository messages;

    @Autowired
    private JdbcTemplate jdbc;

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void flywayCreatesGtConnectCoreTables() {

        Integer count =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM information_schema.tables
                        WHERE table_schema = 'public'
                          AND table_name IN (
                              'gt_connect_spaces',
                              'gt_connect_space_members',
                              'gt_connect_messages'
                          )
                        """,
                        Integer.class
                );

        assertThat(count)
                .isEqualTo(3);

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '150'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(migrationCount)
                .isEqualTo(1);
    }

    @Test
    void persistsCoreConversationRecordsAndReadsThemWithinTenant() {

        UUID tenantId =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        useTenant(
                tenantId,
                "gt-connect-persistence-test"
        );

        ConnectSpace space =
                spaces.saveAndFlush(
                        new ConnectSpace(
                                tenantId,
                                ConnectSpaceType.GROUP,
                                "GT Connect Test Group",
                                "TEST",
                                "GROUP-001"
                        )
                );

        ConnectSpaceMember member =
                members.saveAndFlush(
                        new ConnectSpaceMember(
                                tenantId,
                                space.getId(),
                                userId,
                                ConnectMemberRole.OWNER
                        )
                );

        ConnectMessage message =
                messages.saveAndFlush(
                        new ConnectMessage(
                                tenantId,
                                space.getId(),
                                userId,
                                ConnectMessageType.TEXT,
                                "Hello GT Connect",
                                null
                        )
                );

        assertThat(
                spaces.findByIdAndTenantId(
                        space.getId(),
                        tenantId
                )
        ).isPresent();

        assertThat(
                members
                        .findByTenantIdAndSpaceIdAndUserIdAndMembershipStatus(
                                tenantId,
                                space.getId(),
                                userId,
                                ConnectMembershipStatus.ACTIVE
                        )
        ).isPresent();

        assertThat(
                messages
                        .findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                                tenantId,
                                space.getId()
                        )
        )
                .extracting(
                        ConnectMessage::getId
                )
                .containsExactly(
                        message.getId()
                );

        assertThat(member.getSpaceId())
                .isEqualTo(space.getId());

        assertThat(message.getSpaceId())
                .isEqualTo(space.getId());
    }

    @Test
    void tenantScopedRepositoryQueriesDoNotExposeAnotherTenant() {

        UUID firstTenant =
                UUID.randomUUID();

        UUID secondTenant =
                UUID.randomUUID();

        UUID userId =
                UUID.randomUUID();

        useTenant(
                firstTenant,
                "gt-connect-tenant-a"
        );

        ConnectSpace space =
                spaces.saveAndFlush(
                        new ConnectSpace(
                                firstTenant,
                                ConnectSpaceType.DIRECT,
                                null,
                                null,
                                null
                        )
                );

        ConnectSpaceMember member =
                members.saveAndFlush(
                        new ConnectSpaceMember(
                                firstTenant,
                                space.getId(),
                                userId,
                                ConnectMemberRole.MEMBER
                        )
                );

        ConnectMessage message =
                messages.saveAndFlush(
                        new ConnectMessage(
                                firstTenant,
                                space.getId(),
                                userId,
                                ConnectMessageType.TEXT,
                                "Tenant A message",
                                null
                        )
                );

        useTenant(
                secondTenant,
                "gt-connect-tenant-b"
        );

        assertThat(
                spaces.findByIdAndTenantId(
                        space.getId(),
                        secondTenant
                )
        ).isEmpty();

        assertThat(
                members.findByIdAndTenantId(
                        member.getId(),
                        secondTenant
                )
        ).isEmpty();

        assertThat(
                messages.findByIdAndTenantId(
                        message.getId(),
                        secondTenant
                )
        ).isEmpty();

        assertThat(
                messages
                        .findAllByTenantIdAndSpaceIdOrderBySentAtAsc(
                                secondTenant,
                                space.getId()
                        )
        ).isEmpty();
    }

    @Test
    void databaseRejectsCrossTenantMessageSpaceReference() {

        UUID tenantA =
                UUID.randomUUID();

        UUID tenantB =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        String auditUser =
                "gt-connect-cross-tenant-test";

        jdbc.update(
                """
                INSERT INTO gt_connect_spaces (
                    id,
                    tenant_id,
                    space_type,
                    name,
                    context_type,
                    context_reference,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?,
                    'GROUP',
                    'Tenant A Space',
                    NULL,
                    NULL,
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0,
                    'ACTIVE'
                )
                """,
                spaceId,
                tenantA,
                auditUser,
                auditUser
        );

        assertThatThrownBy(
                () -> jdbc.update(
                        """
                        INSERT INTO gt_connect_messages (
                            id,
                            tenant_id,
                            space_id,
                            sender_user_id,
                            message_type,
                            body,
                            reply_to_message_id,
                            sent_at,
                            edited_at,
                            deleted_at,
                            created_at,
                            created_by,
                            updated_at,
                            updated_by,
                            version,
                            status
                        )
                        VALUES (
                            ?, ?, ?, ?,
                            'TEXT',
                            'Cross-tenant message',
                            NULL,
                            CURRENT_TIMESTAMP,
                            NULL,
                            NULL,
                            CURRENT_TIMESTAMP,
                            ?,
                            CURRENT_TIMESTAMP,
                            ?,
                            0,
                            'ACTIVE'
                        )
                        """,
                        UUID.randomUUID(),
                        tenantB,
                        spaceId,
                        UUID.randomUUID(),
                        auditUser,
                        auditUser
                )
        )
                .isInstanceOf(
                        DataIntegrityViolationException.class
                );
    }

    @Test
    void flywayRegistersGtConnectPermissionsForSchoolAdmin() {

        Integer permissionCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_permission p
                        JOIN eiam_tenant t
                          ON t.id = p.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND p.code IN (
                              'core.connect.manage',
                              'core.connect.moderate',
                              'core.announcements.send'
                          )
                          AND p.module = 'ENTERPRISE_CONNECT'
                          AND p.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(permissionCount)
                .isEqualTo(3);

        Integer schoolAdminRoleCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role r
                        JOIN eiam_tenant t
                          ON t.id = r.tenant_id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'SCHOOL_ADMIN'
                          AND r.status = 'ACTIVE'
                          AND r.system_role = TRUE
                        """,
                        Integer.class
                );

        assertThat(schoolAdminRoleCount)
                .isEqualTo(1);

        Integer assignmentCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role_permission rp
                        JOIN eiam_tenant t
                          ON t.id = rp.tenant_id
                        JOIN eiam_role r
                          ON r.id = rp.role_id
                         AND r.tenant_id = t.id
                        JOIN eiam_permission p
                          ON p.id = rp.permission_id
                         AND p.tenant_id = t.id
                        WHERE t.code = 'GT-SCHOOL'
                          AND r.code = 'SCHOOL_ADMIN'
                          AND p.code IN (
                              'core.connect.manage',
                              'core.connect.moderate',
                              'core.announcements.send'
                          )
                          AND rp.status = 'ACTIVE'
                        """,
                        Integer.class
                );

        assertThat(assignmentCount)
                .isEqualTo(3);

        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '151'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertThat(migrationCount)
                .isEqualTo(1);
    }

    private static void useTenant(
            UUID tenantId,
            String correlationId
    ) {
        RequestContextHolder.set(
                new RequestContext(
                        correlationId,
                        tenantId.toString()
                )
        );
    }
}
