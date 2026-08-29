package africa.growtogether.platform.connect;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationVersion;

import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class ConnectMemberRoleAuthorityBackfillPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_connect_role_authority_test"
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

        /*
         * Boot deliberately one migration before V172.
         */
        registry.add(
                "spring.flyway.target",
                () -> "171"
        );

        registry.add(
                "spring.flyway.enabled",
                () -> "true"
        );

        /*
         * Current ConnectSpaceMember contains V172 fields.
         * Historical V171 schema intentionally does not.
         */
        registry.add(
                "spring.jpa.hibernate.ddl-auto",
                () -> "none"
        );

        registry.add(
                "spring.data.redis.repositories.enabled",
                () -> "false"
        );
    }


    @Autowired
    private JdbcTemplate jdbc;


    @Test
    void migration172AddsProvenanceAndNormalizesOnlyEligibleHistoricalAdmin() {

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID schoolAdminRoleId =
                UUID.randomUUID();


        UUID eligibleAdminUserId =
                UUID.randomUUID();

        UUID ordinaryAdminUserId =
                UUID.randomUUID();

        UUID suspendedAdminUserId =
                UUID.randomUUID();

        UUID eligibleMemberUserId =
                UUID.randomUUID();


        createTenantFixture(
                tenantId
        );

        createSchoolProfile(
                tenantId,
                schoolProfileId
        );

        createSchoolAdminRole(
                tenantId,
                schoolAdminRoleId
        );

        createUser(
                tenantId,
                eligibleAdminUserId,
                "eligible-admin",
                "ACTIVE"
        );

        createUser(
                tenantId,
                ordinaryAdminUserId,
                "ordinary-admin",
                "ACTIVE"
        );

        createUser(
                tenantId,
                suspendedAdminUserId,
                "suspended-admin",
                "SUSPENDED"
        );

        createUser(
                tenantId,
                eligibleMemberUserId,
                "eligible-member",
                "ACTIVE"
        );


        assignSchoolAdmin(
                tenantId,
                eligibleAdminUserId,
                schoolAdminRoleId
        );

        assignSchoolAdmin(
                tenantId,
                suspendedAdminUserId,
                schoolAdminRoleId
        );

        assignSchoolAdmin(
                tenantId,
                eligibleMemberUserId,
                schoolAdminRoleId
        );


        createCanonicalSchoolSpace(
                tenantId,
                schoolProfileId,
                spaceId
        );


        UUID eligibleAdminMembershipId =
                createMembership(
                        tenantId,
                        spaceId,
                        eligibleAdminUserId,
                        "ADMIN"
                );

        UUID ordinaryAdminMembershipId =
                createMembership(
                        tenantId,
                        spaceId,
                        ordinaryAdminUserId,
                        "ADMIN"
                );

        UUID suspendedAdminMembershipId =
                createMembership(
                        tenantId,
                        spaceId,
                        suspendedAdminUserId,
                        "ADMIN"
                );

        UUID eligibleMemberMembershipId =
                createMembership(
                        tenantId,
                        spaceId,
                        eligibleMemberUserId,
                        "MEMBER"
                );


        /*
         * Confirm the historical state really is V171.
         */
        assertEquals(
                0,
                columnCount(
                        "role_authority_source"
                )
        );

        assertEquals(
                0,
                columnCount(
                        "previous_member_role"
                )
        );


        Flyway migration =
                Flyway.configure()
                        .dataSource(
                                POSTGRES.getJdbcUrl(),
                                POSTGRES.getUsername(),
                                POSTGRES.getPassword()
                        )
                        .target(
                                MigrationVersion.fromVersion(
                                        "172"
                                )
                        )
                        .load();

        migration.migrate();


        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '172'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertEquals(
                1,
                migrationCount
        );


        assertEquals(
                1,
                columnCount(
                        "role_authority_source"
                )
        );

        assertEquals(
                1,
                columnCount(
                        "previous_member_role"
                )
        );


        Integer provenanceConstraintCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM pg_constraint
                        WHERE conname IN (
                            'ck_gt_connect_role_authority_source',
                            'ck_gt_connect_previous_member_role',
                            'ck_gt_connect_previous_role_requires_authority'
                        )
                        """,
                        Integer.class
                );

        assertEquals(
                3,
                provenanceConstraintCount
        );


        /*
         * Eligible historical SCHOOL_ADMIN ADMIN is normalized.
         */
        assertEquals(
                "EIAM_SCHOOL_ADMIN",
                authoritySource(
                        eligibleAdminMembershipId
                )
        );

        assertNull(
                previousRole(
                        eligibleAdminMembershipId
                )
        );


        /*
         * Active ADMIN without SCHOOL_ADMIN remains ordinary.
         */
        assertNull(
                authoritySource(
                        ordinaryAdminMembershipId
                )
        );


        /*
         * Suspended EIAM user must not retain derived authority
         * through historical normalization.
         */
        assertNull(
                authoritySource(
                        suspendedAdminMembershipId
                )
        );


        /*
         * SCHOOL_ADMIN assignment alone must not rewrite an ordinary
         * MEMBER historical row. Runtime reconciliation performs the
         * controlled MEMBER -> ADMIN promotion.
         */
        assertNull(
                authoritySource(
                        eligibleMemberMembershipId
                )
        );

        assertEquals(
                "MEMBER",
                memberRole(
                        eligibleMemberMembershipId
                )
        );


        Integer normalizedCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gt_connect_space_members
                        WHERE tenant_id = ?
                          AND role_authority_source = 'EIAM_SCHOOL_ADMIN'
                        """,
                        Integer.class,
                        tenantId
                );

        assertEquals(
                1,
                normalizedCount
        );
    }


    private void createTenantFixture(
            UUID tenantId
    ) {

        UUID organizationId =
                UUID.randomUUID();

        String suffix =
                tenantId
                        .toString()
                        .replace("-", "")
                        .substring(0, 12)
                        .toUpperCase();

        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    CURRENT_TIMESTAMP
                )
                """,
                organizationId,
                "ROLE-AUTH-ORG-" + suffix,
                "GT Connect Role Authority Test Organization"
        );

        jdbc.update(
                """
                INSERT INTO eiam_tenant (
                    id,
                    organization_id,
                    code,
                    name,
                    status,
                    created_at,
                    version
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    0
                )
                """,
                tenantId,
                organizationId,
                "ROLE-AUTH-TENANT-" + suffix,
                "GT Connect Role Authority Test Tenant"
        );
    }


    private void createSchoolProfile(
            UUID tenantId,
            UUID schoolProfileId
    ) {

        jdbc.update(
                """
                INSERT INTO gts_school_profile (
                    id,
                    tenant_id,
                    school_code,
                    school_name,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?,
                    ?,
                    'ROLE-AUTH-SCHOOL',
                    'GT Connect Role Authority School',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0
                )
                """,
                schoolProfileId,
                tenantId
        );
    }


    private void createSchoolAdminRole(
            UUID tenantId,
            UUID roleId
    ) {

        jdbc.update(
                """
                INSERT INTO eiam_role (
                    id,
                    tenant_id,
                    code,
                    name,
                    description,
                    system_role,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?,
                    ?,
                    'SCHOOL_ADMIN',
                    'School Administrator',
                    'Test School administrator role',
                    TRUE,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                roleId,
                tenantId
        );
    }


    private void createUser(
            UUID tenantId,
            UUID userId,
            String username,
            String accountStatus
    ) {

        jdbc.update(
                """
                INSERT INTO eiam_user_account (
                    id,
                    tenant_id,
                    username,
                    email,
                    display_name,
                    password_hash,
                    account_status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    'test-password-hash',
                    ?,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                userId,
                tenantId,
                username,
                username + "@example.com",
                username,
                accountStatus
        );
    }


    private void assignSchoolAdmin(
            UUID tenantId,
            UUID userId,
            UUID roleId
    ) {

        jdbc.update(
                """
                INSERT INTO eiam_user_role (
                    id,
                    tenant_id,
                    user_id,
                    role_id,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                UUID.randomUUID(),
                tenantId,
                userId,
                roleId
        );
    }


    private void createCanonicalSchoolSpace(
            UUID tenantId,
            UUID schoolProfileId,
            UUID spaceId
    ) {

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
                    ?,
                    ?,
                    'INSTITUTION',
                    'GT Connect Role Authority School',
                    'SCHOOL_PROFILE',
                    ?,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                spaceId,
                tenantId,
                schoolProfileId.toString()
        );
    }


    private UUID createMembership(
            UUID tenantId,
            UUID spaceId,
            UUID userId,
            String memberRole
    ) {

        UUID membershipId =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO gt_connect_space_members (
                    id,
                    tenant_id,
                    space_id,
                    user_id,
                    member_role,
                    joined_at,
                    membership_status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?,
                    ?,
                    ?,
                    ?,
                    ?,
                    CURRENT_TIMESTAMP,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                membershipId,
                tenantId,
                spaceId,
                userId,
                memberRole
        );

        return membershipId;
    }


    private Integer columnCount(
            String columnName
    ) {

        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM information_schema.columns
                WHERE table_schema = 'public'
                  AND table_name = 'gt_connect_space_members'
                  AND column_name = ?
                """,
                Integer.class,
                columnName
        );
    }


    private String authoritySource(
            UUID membershipId
    ) {

        return jdbc.queryForObject(
                """
                SELECT role_authority_source
                FROM gt_connect_space_members
                WHERE id = ?
                """,
                String.class,
                membershipId
        );
    }


    private String previousRole(
            UUID membershipId
    ) {

        return jdbc.queryForObject(
                """
                SELECT previous_member_role
                FROM gt_connect_space_members
                WHERE id = ?
                """,
                String.class,
                membershipId
        );
    }


    private String memberRole(
            UUID membershipId
    ) {

        return jdbc.queryForObject(
                """
                SELECT member_role
                FROM gt_connect_space_members
                WHERE id = ?
                """,
                String.class,
                membershipId
        );
    }
}
