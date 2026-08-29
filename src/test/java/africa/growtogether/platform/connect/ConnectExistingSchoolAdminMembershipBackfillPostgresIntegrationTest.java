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
class ConnectExistingSchoolAdminMembershipBackfillPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_connect_admin_backfill_test"
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
         * Boot deliberately at the schema immediately before V173.
         */
        registry.add(
                "spring.flyway.target",
                () -> "172"
        );

        registry.add(
                "spring.flyway.enabled",
                () -> "true"
        );

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
    void migration173ReconcilesOnlyEligibleHistoricalSchoolAdministrators() {

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        UUID spaceId =
                UUID.randomUUID();

        UUID schoolAdminRoleId =
                UUID.randomUUID();


        UUID missingMembershipUserId =
                UUID.randomUUID();

        UUID existingMemberUserId =
                UUID.randomUUID();

        UUID existingAdminUserId =
                UUID.randomUUID();

        UUID suspendedAdminUserId =
                UUID.randomUUID();

        UUID nonSchoolAdminUserId =
                UUID.randomUUID();

        UUID otherAuthorityUserId =
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

        createCanonicalSchoolSpace(
                tenantId,
                schoolProfileId,
                spaceId
        );


        createUser(
                tenantId,
                missingMembershipUserId,
                "missing-membership-admin",
                "ACTIVE"
        );

        createUser(
                tenantId,
                existingMemberUserId,
                "existing-member-admin",
                "ACTIVE"
        );

        createUser(
                tenantId,
                existingAdminUserId,
                "existing-admin",
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
                nonSchoolAdminUserId,
                "ordinary-user",
                "ACTIVE"
        );

        createUser(
                tenantId,
                otherAuthorityUserId,
                "other-authority-admin",
                "ACTIVE"
        );


        assignSchoolAdmin(
                tenantId,
                missingMembershipUserId,
                schoolAdminRoleId
        );

        assignSchoolAdmin(
                tenantId,
                existingMemberUserId,
                schoolAdminRoleId
        );

        assignSchoolAdmin(
                tenantId,
                existingAdminUserId,
                schoolAdminRoleId
        );

        assignSchoolAdmin(
                tenantId,
                suspendedAdminUserId,
                schoolAdminRoleId
        );

        assignSchoolAdmin(
                tenantId,
                otherAuthorityUserId,
                schoolAdminRoleId
        );


        UUID existingMemberMembershipId =
                createMembership(
                        tenantId,
                        spaceId,
                        existingMemberUserId,
                        "MEMBER",
                        null
                );

        UUID existingAdminMembershipId =
                createMembership(
                        tenantId,
                        spaceId,
                        existingAdminUserId,
                        "ADMIN",
                        null
                );

        UUID ordinaryMembershipId =
                createMembership(
                        tenantId,
                        spaceId,
                        nonSchoolAdminUserId,
                        "MEMBER",
                        null
                );

        UUID otherAuthorityMembershipId =
                createMembership(
                        tenantId,
                        spaceId,
                        otherAuthorityUserId,
                        "MEMBER",
                        "EIAM_OTHER_AUTHORITY"
                );


        assertEquals(
                0,
                activeMembershipCount(
                        tenantId,
                        spaceId,
                        missingMembershipUserId
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
                                        "173"
                                )
                        )
                        .load();

        migration.migrate();


        Integer migrationCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM flyway_schema_history
                        WHERE version = '173'
                          AND success = TRUE
                        """,
                        Integer.class
                );

        assertEquals(
                1,
                migrationCount
        );


        /*
         * Missing historical membership is created.
         */
        assertEquals(
                1,
                activeMembershipCount(
                        tenantId,
                        spaceId,
                        missingMembershipUserId
                )
        );

        assertEquals(
                "ADMIN",
                activeMemberRole(
                        tenantId,
                        spaceId,
                        missingMembershipUserId
                )
        );

        assertEquals(
                "EIAM_SCHOOL_ADMIN",
                activeAuthoritySource(
                        tenantId,
                        spaceId,
                        missingMembershipUserId
                )
        );

        assertNull(
                activePreviousRole(
                        tenantId,
                        spaceId,
                        missingMembershipUserId
                )
        );

        assertEquals(
                "system",
                activeCreatedBy(
                        tenantId,
                        spaceId,
                        missingMembershipUserId
                )
        );


        /*
         * Existing MEMBER becomes authoritative ADMIN and preserves
         * MEMBER for later restoration.
         */
        assertEquals(
                "ADMIN",
                memberRole(
                        existingMemberMembershipId
                )
        );

        assertEquals(
                "EIAM_SCHOOL_ADMIN",
                authoritySource(
                        existingMemberMembershipId
                )
        );

        assertEquals(
                "MEMBER",
                previousRole(
                        existingMemberMembershipId
                )
        );


        /*
         * Existing ordinary ADMIN receives provenance but no false
         * previous role.
         */
        assertEquals(
                "ADMIN",
                memberRole(
                        existingAdminMembershipId
                )
        );

        assertEquals(
                "EIAM_SCHOOL_ADMIN",
                authoritySource(
                        existingAdminMembershipId
                )
        );

        assertNull(
                previousRole(
                        existingAdminMembershipId
                )
        );


        /*
         * Suspended SCHOOL_ADMIN is excluded.
         */
        assertEquals(
                0,
                activeMembershipCount(
                        tenantId,
                        spaceId,
                        suspendedAdminUserId
                )
        );


        /*
         * Active user without SCHOOL_ADMIN is untouched.
         */
        assertEquals(
                "MEMBER",
                memberRole(
                        ordinaryMembershipId
                )
        );

        assertNull(
                authoritySource(
                        ordinaryMembershipId
                )
        );


        /*
         * An existing membership governed by another authority source
         * must not be overwritten.
         */
        assertEquals(
                "MEMBER",
                memberRole(
                        otherAuthorityMembershipId
                )
        );

        assertEquals(
                "EIAM_OTHER_AUTHORITY",
                authoritySource(
                        otherAuthorityMembershipId
                )
        );

        assertNull(
                previousRole(
                        otherAuthorityMembershipId
                )
        );


        Integer schoolAdminAuthorityCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gt_connect_space_members
                        WHERE tenant_id = ?
                          AND space_id = ?
                          AND membership_status = 'ACTIVE'
                          AND role_authority_source = 'EIAM_SCHOOL_ADMIN'
                        """,
                        Integer.class,
                        tenantId,
                        spaceId
                );

        assertEquals(
                3,
                schoolAdminAuthorityCount
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
                "ADMIN-BACKFILL-ORG-" + suffix,
                "GT Connect Admin Backfill Test Organization"
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
                "ADMIN-BACKFILL-TENANT-" + suffix,
                "GT Connect Admin Backfill Test Tenant"
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
                    'ADMIN-BACKFILL-SCHOOL',
                    'GT Connect Admin Backfill School',
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
                    'GT Connect Admin Backfill School',
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
            String memberRole,
            String authoritySource
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
                    role_authority_source,
                    previous_member_role,
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
                    ?,
                    NULL,
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
                memberRole,
                authoritySource
        );

        return membershipId;
    }


    private Integer activeMembershipCount(
            UUID tenantId,
            UUID spaceId,
            UUID userId
    ) {

        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM gt_connect_space_members
                WHERE tenant_id = ?
                  AND space_id = ?
                  AND user_id = ?
                  AND membership_status = 'ACTIVE'
                """,
                Integer.class,
                tenantId,
                spaceId,
                userId
        );
    }


    private String activeMemberRole(
            UUID tenantId,
            UUID spaceId,
            UUID userId
    ) {

        return jdbc.queryForObject(
                """
                SELECT member_role
                FROM gt_connect_space_members
                WHERE tenant_id = ?
                  AND space_id = ?
                  AND user_id = ?
                  AND membership_status = 'ACTIVE'
                """,
                String.class,
                tenantId,
                spaceId,
                userId
        );
    }


    private String activeAuthoritySource(
            UUID tenantId,
            UUID spaceId,
            UUID userId
    ) {

        return jdbc.queryForObject(
                """
                SELECT role_authority_source
                FROM gt_connect_space_members
                WHERE tenant_id = ?
                  AND space_id = ?
                  AND user_id = ?
                  AND membership_status = 'ACTIVE'
                """,
                String.class,
                tenantId,
                spaceId,
                userId
        );
    }


    private String activePreviousRole(
            UUID tenantId,
            UUID spaceId,
            UUID userId
    ) {

        return jdbc.queryForObject(
                """
                SELECT previous_member_role
                FROM gt_connect_space_members
                WHERE tenant_id = ?
                  AND space_id = ?
                  AND user_id = ?
                  AND membership_status = 'ACTIVE'
                """,
                String.class,
                tenantId,
                spaceId,
                userId
        );
    }


    private String activeCreatedBy(
            UUID tenantId,
            UUID spaceId,
            UUID userId
    ) {

        return jdbc.queryForObject(
                """
                SELECT created_by
                FROM gt_connect_space_members
                WHERE tenant_id = ?
                  AND space_id = ?
                  AND user_id = ?
                  AND membership_status = 'ACTIVE'
                """,
                String.class,
                tenantId,
                spaceId,
                userId
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
}
