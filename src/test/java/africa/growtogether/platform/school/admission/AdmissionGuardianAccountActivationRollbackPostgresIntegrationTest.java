package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;

import africa.growtogether.platform.eiam.membership.AcceptInvitationCommand;

import org.junit.jupiter.api.AfterEach;
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

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.sql.Date;
import java.util.HexFormat;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class AdmissionGuardianAccountActivationRollbackPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_guardian_activation_rollback_test"
                    )
                    .withUsername(
                            "gt_test"
                    )
                    .withPassword(
                            "gt_test"
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
    AdmissionGuardianAccountProvisioningService service;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void clearContext() {
        RequestContextHolder.clear();
    }

    @Test
    void mismatchedAcceptedInvitationRollsBackEntireCrossServiceTransaction() {

        Fixture fixture =
                createFixture(
                        "rollback"
                );

        String suffix =
                shortId(
                        UUID.randomUUID()
                ).toLowerCase();

        String actualEmail =
                "actual-"
                        + suffix
                        + "@example.com";

        String rawToken =
                "rollback-token-"
                        + UUID.randomUUID();

        UUID actualInvitationId =
                createInvitation(
                        fixture.tenantId(),
                        actualEmail,
                        rawToken
                );

        /*
         * Prove the database preconditions before exercising the
         * production transaction.
         */
        assertEquals(
                "PENDING",
                invitationStatus(
                        actualInvitationId
                )
        );

        assertEquals(
                "PENDING_ACTIVATION",
                provisioningStatus(
                        fixture.provisioningId()
                )
        );

        assertEquals(
                0,
                countUsersByEmail(
                        fixture.tenantId(),
                        actualEmail
                )
        );

        assertEquals(
                0,
                countMemberships(
                        fixture.tenantId()
                )
        );

        setTenant(
                fixture.tenantId()
        );

        AdmissionGuardianAccountProvisioningException error =
                assertThrows(
                        AdmissionGuardianAccountProvisioningException.class,
                        () ->
                                service.completeParentAccountActivation(
                                        fixture.provisioningId(),
                                        new AcceptInvitationCommand(
                                                rawToken,
                                                "rollback-parent-" + suffix,
                                                "Rollback Parent",
                                                "strong-password-123"
                                        )
                                )
                );

        assertEquals(
                "Accepted EIAM invitation does not belong to this guardian account provisioning",
                error.getMessage()
        );

        /*
         * The EIAM acceptance happens before the school-side
         * invitation correlation check.
         *
         * If the enclosing transaction is correct, every EIAM write
         * made by acceptWithEvidence() is rolled back when the
         * correlation mismatch throws.
         */

        assertEquals(
                "PENDING",
                invitationStatus(
                        actualInvitationId
                )
        );

        assertEquals(
                0,
                invitationAcceptedEvidenceCount(
                        actualInvitationId
                )
        );

        assertEquals(
                "PENDING_ACTIVATION",
                provisioningStatus(
                        fixture.provisioningId()
                )
        );

        assertEquals(
                0,
                provisioningActivationEvidenceCount(
                        fixture.provisioningId()
                )
        );

        assertEquals(
                0,
                countUsersByEmail(
                        fixture.tenantId(),
                        actualEmail
                )
        );

        assertEquals(
                0,
                countMemberships(
                        fixture.tenantId()
                )
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
                "a12-activation-rollback-test";

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

        UUID expectedInvitationId =
                UUID.randomUUID();

        UUID provisioningId =
                UUID.randomUUID();

        String expectedEmail =
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
                "Activation Rollback Organisation " + suffix
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
                "Activation Rollback Tenant " + suffix
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
                "Activation Rollback Test School",
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

        /*
         * The V156 record points to this invitation.
         * The activation command below will deliberately use another
         * valid invitation.
         */
        insertInvitation(
                expectedInvitationId,
                tenantId,
                expectedEmail,
                sha256(
                        "expected-unused-token-" + suffix
                )
        );

        jdbc.update(
                """
                INSERT INTO gts_admission_guardian_account_provisioning (
                    id,
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
                    ?, ?, ?,
                    'SECURE_INVITATION',
                    'EMAIL',
                    ?,
                    ?,
                    'PENDING_ACTIVATION',
                    CURRENT_TIMESTAMP,
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0,
                    'ACTIVE'
                )
                """,
                provisioningId,
                tenantId,
                guardianId,
                expectedEmail,
                expectedInvitationId,
                auditUser,
                auditUser
        );

        return new Fixture(
                tenantId,
                provisioningId
        );
    }

    private UUID createInvitation(
            UUID tenantId,
            String email,
            String rawToken
    ) {

        UUID invitationId =
                UUID.randomUUID();

        insertInvitation(
                invitationId,
                tenantId,
                email,
                sha256(
                        rawToken
                )
        );

        return invitationId;
    }

    private void insertInvitation(
            UUID invitationId,
            UUID tenantId,
            String email,
            String tokenHash
    ) {

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
                    ?, ?, ?,
                    NULL,
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
                tokenHash
        );
    }

    private String invitationStatus(
            UUID invitationId
    ) {

        return jdbc.queryForObject(
                """
                SELECT invitation_status
                FROM eiam_organization_invitation
                WHERE id = ?
                """,
                String.class,
                invitationId
        );
    }

    private String provisioningStatus(
            UUID provisioningId
    ) {

        return jdbc.queryForObject(
                """
                SELECT provisioning_status
                FROM gts_admission_guardian_account_provisioning
                WHERE id = ?
                """,
                String.class,
                provisioningId
        );
    }

    private int invitationAcceptedEvidenceCount(
            UUID invitationId
    ) {

        Integer result =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_organization_invitation
                        WHERE id = ?
                          AND (
                              invitation_status <> 'PENDING'
                              OR accepted_at IS NOT NULL
                          )
                        """,
                        Integer.class,
                        invitationId
                );

        return result == null ? 0 : result;
    }

    private int provisioningActivationEvidenceCount(
            UUID provisioningId
    ) {

        Integer result =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM gts_admission_guardian_account_provisioning
                        WHERE id = ?
                          AND (
                              provisioning_status <> 'PENDING_ACTIVATION'
                              OR eiam_user_id IS NOT NULL
                              OR activated_at IS NOT NULL
                          )
                        """,
                        Integer.class,
                        provisioningId
                );

        return result == null ? 0 : result;
    }

    private int countUsersByEmail(
            UUID tenantId,
            String email
    ) {

        Integer result =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_user_account
                        WHERE tenant_id = ?
                          AND LOWER(email) = LOWER(?)
                        """,
                        Integer.class,
                        tenantId,
                        email
                );

        return result == null ? 0 : result;
    }

    private int countMemberships(
            UUID tenantId
    ) {

        Integer result =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_tenant_membership
                        WHERE tenant_id = ?
                        """,
                        Integer.class,
                        tenantId
                );

        return result == null ? 0 : result;
    }

    private static void setTenant(
            UUID tenantId
    ) {

        RequestContextHolder.set(
                new RequestContext(
                        "test",
                        tenantId.toString()
                )
        );
    }

    private static String sha256(
            String value
    ) {

        try {

            return HexFormat
                    .of()
                    .formatHex(
                            MessageDigest
                                    .getInstance(
                                            "SHA-256"
                                    )
                                    .digest(
                                            value.getBytes(
                                                    StandardCharsets.UTF_8
                                            )
                                    )
                    );

        } catch (NoSuchAlgorithmException exception) {

            throw new IllegalStateException(
                    "SHA-256 is unavailable.",
                    exception
            );
        }
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
            UUID provisioningId
    ) {
    }
}
