package africa.growtogether.platform.school.admission;

import org.flywaydb.core.Flyway;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Date;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers
class AdmissionGuardianAccountProvisioningPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_guardian_provisioning_test"
                    )
                    .withUsername(
                            "gt_test"
                    )
                    .withPassword(
                            "gt_test"
                    );

    private static JdbcTemplate jdbc;

    @BeforeAll
    static void migrateDatabase() {

        Flyway.configure()
                .dataSource(
                        POSTGRES.getJdbcUrl(),
                        POSTGRES.getUsername(),
                        POSTGRES.getPassword()
                )
                .locations(
                        "classpath:db/migration"
                )
                .load()
                .migrate();

        DriverManagerDataSource dataSource =
                new DriverManagerDataSource();

        dataSource.setUrl(
                POSTGRES.getJdbcUrl()
        );

        dataSource.setUsername(
                POSTGRES.getUsername()
        );

        dataSource.setPassword(
                POSTGRES.getPassword()
        );

        jdbc =
                new JdbcTemplate(
                        dataSource
                );
    }

    @Test
    void storesSecureInvitationProvisioningEvidence() {

        Fixture fixture =
                createFixture(
                        "secure"
                );

        insertSecureInvitationProvisioning(
                fixture.tenantId(),
                fixture.guardianId(),
                fixture.invitationId(),
                "EMAIL",
                fixture.email()
        );

        String status =
                jdbc.queryForObject(
                        """
                        SELECT provisioning_status
                        FROM gts_admission_guardian_account_provisioning
                        WHERE tenant_id = ?
                          AND admission_guardian_id = ?
                        """,
                        String.class,
                        fixture.tenantId(),
                        fixture.guardianId()
                );

        assertEquals(
                "PENDING_ACTIVATION",
                status
        );
    }

    @Test
    void rejectsDuplicateProvisioningForSameAdmissionGuardian() {

        Fixture fixture =
                createFixture(
                        "duplicate"
                );

        insertSecureInvitationProvisioning(
                fixture.tenantId(),
                fixture.guardianId(),
                fixture.invitationId(),
                "EMAIL",
                fixture.email()
        );

        UUID secondInvitationId =
                createInvitation(
                        fixture.tenantId(),
                        "second-" + fixture.email(),
                        null
                );

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        insertSecureInvitationProvisioning(
                                fixture.tenantId(),
                                fixture.guardianId(),
                                secondInvitationId,
                                "EMAIL",
                                "second-" + fixture.email()
                        )
        );
    }

    @Test
    void rejectsCrossTenantGuardianAndInvitationReferences() {

        Fixture first =
                createFixture(
                        "cross-a"
                );

        Fixture second =
                createFixture(
                        "cross-b"
                );

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        insertSecureInvitationProvisioning(
                                first.tenantId(),
                                second.guardianId(),
                                first.invitationId(),
                                "EMAIL",
                                first.email()
                        )
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        insertSecureInvitationProvisioning(
                                first.tenantId(),
                                first.guardianId(),
                                second.invitationId(),
                                "EMAIL",
                                second.email()
                        )
        );
    }

    @Test
    void storesExistingIdentityReuseAsActivated() {

        Fixture fixture =
                createFixture(
                        "reuse"
                );

        insertExistingIdentityReuse(
                fixture.tenantId(),
                fixture.guardianId(),
                fixture.userId()
        );

        String method =
                jdbc.queryForObject(
                        """
                        SELECT provisioning_method
                        FROM gts_admission_guardian_account_provisioning
                        WHERE tenant_id = ?
                          AND admission_guardian_id = ?
                        """,
                        String.class,
                        fixture.tenantId(),
                        fixture.guardianId()
                );

        String status =
                jdbc.queryForObject(
                        """
                        SELECT provisioning_status
                        FROM gts_admission_guardian_account_provisioning
                        WHERE tenant_id = ?
                          AND admission_guardian_id = ?
                        """,
                        String.class,
                        fixture.tenantId(),
                        fixture.guardianId()
                );

        assertEquals(
                "EXISTING_IDENTITY_REUSE",
                method
        );

        assertEquals(
                "ACTIVATED",
                status
        );
    }

    @Test
    void rejectsCrossTenantEiamUserReference() {

        Fixture first =
                createFixture(
                        "user-a"
                );

        Fixture second =
                createFixture(
                        "user-b"
                );

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        insertExistingIdentityReuse(
                                first.tenantId(),
                                first.guardianId(),
                                second.userId()
                        )
        );
    }

    @Test
    void rejectsSecureInvitationWithoutInvitationEvidence() {

        Fixture fixture =
                createFixture(
                        "missing-evidence"
                );

        assertThrows(
                DataIntegrityViolationException.class,
                () ->
                        jdbc.update(
                                """
                                INSERT INTO gts_admission_guardian_account_provisioning (
                                    tenant_id,
                                    admission_guardian_id,
                                    provisioning_method,
                                    contact_identity_type,
                                    contact_identity,
                                    provisioning_status,
                                    created_at,
                                    created_by,
                                    updated_at,
                                    updated_by,
                                    version,
                                    status
                                )
                                VALUES (
                                    ?, ?,
                                    'SECURE_INVITATION',
                                    'EMAIL',
                                    ?,
                                    'PENDING_ACTIVATION',
                                    CURRENT_TIMESTAMP,
                                    'test',
                                    CURRENT_TIMESTAMP,
                                    'test',
                                    0,
                                    'ACTIVE'
                                )
                                """,
                                fixture.tenantId(),
                                fixture.guardianId(),
                                fixture.email()
                        )
        );
    }

    private void insertSecureInvitationProvisioning(
            UUID tenantId,
            UUID guardianId,
            UUID invitationId,
            String identityType,
            String identity
    ) {

        jdbc.update(
                """
                INSERT INTO gts_admission_guardian_account_provisioning (
                    tenant_id,
                    admission_guardian_id,
                    provisioning_method,
                    contact_identity_type,
                    contact_identity,
                    invitation_id,
                    provisioning_status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?,
                    'SECURE_INVITATION',
                    ?, ?,
                    ?,
                    'PENDING_ACTIVATION',
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                tenantId,
                guardianId,
                identityType,
                identity,
                invitationId
        );
    }

    private void insertExistingIdentityReuse(
            UUID tenantId,
            UUID guardianId,
            UUID userId
    ) {

        jdbc.update(
                """
                INSERT INTO gts_admission_guardian_account_provisioning (
                    tenant_id,
                    admission_guardian_id,
                    provisioning_method,
                    eiam_user_id,
                    provisioning_status,
                    activated_at,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?,
                    'EXISTING_IDENTITY_REUSE',
                    ?,
                    'ACTIVATED',
                    CURRENT_TIMESTAMP,
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                tenantId,
                guardianId,
                userId
        );
    }

    private Fixture createFixture(
            String label
    ) {

        String suffix =
                shortId(
                        UUID.randomUUID()
                );

        String auditUser =
                "a12-gdn-prov-test";

        UUID organizationId =
                UUID.randomUUID();

        UUID tenantId =
                UUID.randomUUID();

        UUID schoolProfileId =
                UUID.randomUUID();

        UUID campusId =
                UUID.randomUUID();

        UUID academicYearId =
                UUID.randomUUID();

        UUID educationLevelId =
                UUID.randomUUID();

        UUID classGradeId =
                UUID.randomUUID();

        UUID applicationId =
                UUID.randomUUID();

        UUID guardianId =
                UUID.randomUUID();

        String email =
                label
                        + "-"
                        + suffix.toLowerCase()
                        + "@example.com";

        jdbc.update(
                """
                INSERT INTO eiam_organization (
                    id,
                    code,
                    name,
                    created_at
                )
                VALUES (?, ?, ?, CURRENT_TIMESTAMP)
                """,
                organizationId,
                "ORG-" + suffix,
                "Guardian Provisioning Organisation " + suffix
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
                    ?, ?, ?, ?,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    0
                )
                """,
                tenantId,
                organizationId,
                "TEN-" + suffix,
                "Guardian Provisioning Tenant " + suffix
        );

        jdbc.update(
                """
                INSERT INTO gts_school_profile (
                    id,
                    tenant_id,
                    school_code,
                    school_name,
                    country_code,
                    default_currency,
                    timezone,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    'UG',
                    'UGX',
                    'Africa/Kampala',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                schoolProfileId,
                tenantId,
                "SCH-" + suffix,
                "Guardian Provisioning Test School",
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_campus (
                    id,
                    tenant_id,
                    school_profile_id,
                    campus_code,
                    campus_name,
                    main_campus,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                campusId,
                tenantId,
                schoolProfileId,
                "MAIN-" + suffix,
                "Main Campus",
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_academic_year (
                    id,
                    tenant_id,
                    academic_year_code,
                    academic_year_name,
                    start_date,
                    end_date,
                    current_year,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    ?, ?,
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                academicYearId,
                tenantId,
                "AY-2026-" + suffix,
                "Academic Year 2026",
                Date.valueOf(
                        "2026-01-01"
                ),
                Date.valueOf(
                        "2026-12-31"
                ),
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_education_level (
                    id,
                    tenant_id,
                    level_code,
                    level_name,
                    sequence_number,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?,
                    1,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                educationLevelId,
                tenantId,
                "PRIMARY-" + suffix,
                "Primary",
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_class_grade (
                    id,
                    tenant_id,
                    education_level_id,
                    class_code,
                    class_name,
                    sequence_number,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?,
                    1,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                classGradeId,
                tenantId,
                educationLevelId,
                "P1-" + suffix,
                "Primary One",
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_admission_application (
                    id,
                    tenant_id,
                    application_number,
                    academic_year_id,
                    campus_id,
                    desired_class_grade_id,
                    application_date,
                    admission_status,
                    submission_channel,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?, ?, ?, ?,
                    CURRENT_DATE,
                    'DRAFT',
                    'OFFICE',
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                applicationId,
                tenantId,
                "APP-" + suffix,
                academicYearId,
                campusId,
                classGradeId,
                auditUser,
                auditUser
        );

        jdbc.update(
                """
                INSERT INTO gts_admission_guardian (
                    id,
                    tenant_id,
                    admission_application_id,
                    relationship_type,
                    first_name,
                    last_name,
                    phone_number,
                    primary_guardian,
                    emergency_contact,
                    authorized_to_collect,
                    receives_communications,
                    financial_responsibility,
                    status,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version
                )
                VALUES (
                    ?, ?, ?,
                    'MOTHER',
                    'Sarah',
                    'Nakato',
                    '+256701234567',
                    TRUE,
                    TRUE,
                    TRUE,
                    TRUE,
                    TRUE,
                    'ACTIVE',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0
                )
                """,
                guardianId,
                tenantId,
                applicationId,
                auditUser,
                auditUser
        );

        UUID invitationId =
                createInvitation(
                        tenantId,
                        email,
                        null
                );

        UUID userId =
                createUser(
                        tenantId,
                        suffix,
                        email
                );

        return new Fixture(
                tenantId,
                guardianId,
                invitationId,
                userId,
                email
        );
    }

    private UUID createInvitation(
            UUID tenantId,
            String email,
            String phone
    ) {

        UUID invitationId =
                UUID.randomUUID();

        jdbc.update(
                """
                INSERT INTO eiam_organization_invitation (
                    id,
                    tenant_id,
                    email,
                    phone_number,
                    token_hash,
                    invitation_status,
                    expires_at,
                    created_at,
                    created_by,
                    updated_at,
                    updated_by,
                    version,
                    status
                )
                VALUES (
                    ?, ?, ?, ?,
                    ?,
                    'PENDING',
                    CURRENT_TIMESTAMP + INTERVAL '7 days',
                    CURRENT_TIMESTAMP,
                    'test',
                    CURRENT_TIMESTAMP,
                    'test',
                    0,
                    'ACTIVE'
                )
                """,
                invitationId,
                tenantId,
                email,
                phone,
                shortId(
                        UUID.randomUUID()
                ).repeat(8)
        );

        return invitationId;
    }

    private UUID createUser(
            UUID tenantId,
            String suffix,
            String email
    ) {

        UUID userId =
                UUID.randomUUID();

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
                    ?, ?, ?, ?, ?,
                    ?,
                    'ACTIVE',
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
                "parent-" + suffix.toLowerCase(),
                email,
                "Parent " + suffix,
                "test-password-hash"
        );

        return userId;
    }

    private static String shortId(
            UUID id
    ) {

        return id
                .toString()
                .replace(
                        "-",
                        ""
                )
                .substring(
                        0,
                        8
                )
                .toUpperCase();
    }

    private record Fixture(
            UUID tenantId,
            UUID guardianId,
            UUID invitationId,
            UUID userId,
            String email
    ) {
    }
}
