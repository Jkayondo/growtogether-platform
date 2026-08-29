package africa.growtogether.platform.school.admission;

import africa.growtogether.platform.common.security.GtPrincipal;
import africa.growtogether.platform.common.web.RequestContext;
import africa.growtogether.platform.common.web.RequestContextHolder;
import africa.growtogether.platform.ens.NotificationSecurePayloadException;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Date;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Testcontainers(disabledWithoutDocker = true)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@SpringBootTest
class AdmissionGuardianActivationNotificationRollbackPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>(
                    "postgres:17-alpine"
            )
                    .withDatabaseName(
                            "growtogether_b8_activation_rollback_test"
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

        /*
         * Deliberately disable the V159 encryption capability.
         *
         * NotificationSecurePayloadService must fail closed after
         * B7 and ordinary ENS writes have joined the outer B8 transaction.
         */
        registry.add(
                "gt.ens.secure-payload.enabled",
                () -> "false"
        );
    }

    @Autowired
    AdmissionGuardianActivationNotificationService service;

    @Autowired
    JdbcTemplate jdbc;

    @AfterEach
    void clearContexts() {
        RequestContextHolder.clear();
        SecurityContextHolder.clearContext();
    }

    @Test
    void securePayloadFailureRollsBackB7AndEnsAtomically() {

        Fixture fixture =
                createFixture();

        setExecutionContext(
                fixture.tenantId(),
                fixture.actorUserId()
        );

        int invitationsBefore =
                countInvitationsByPhone(
                        fixture.tenantId(),
                        fixture.guardianPhone()
                );

        int provisioningsBefore =
                countProvisionings(
                        fixture.tenantId(),
                        fixture.guardianId()
                );

        int notificationsBefore =
                countActivationNotifications(
                        fixture.tenantId(),
                        fixture.guardianPhone()
                );

        int securePayloadsBefore =
                countSecurePayloads(
                        fixture.tenantId()
                );

        assertEquals(
                0,
                invitationsBefore
        );

        assertEquals(
                0,
                provisioningsBefore
        );

        assertEquals(
                0,
                notificationsBefore
        );

        assertThrows(
                NotificationSecurePayloadException.class,
                () ->
                        service.provisionAndNotify(
                                fixture.guardianId()
                        )
        );

        /*
         * The failure occurs at the enterprise secure-payload boundary.
         *
         * Because the B8 orchestration owns the outer transaction,
         * EIAM invitation creation, V156 provisioning and the ordinary
         * ENS request must all disappear with the failed V159 attachment.
         */
        assertEquals(
                invitationsBefore,
                countInvitationsByPhone(
                        fixture.tenantId(),
                        fixture.guardianPhone()
                )
        );

        assertEquals(
                provisioningsBefore,
                countProvisionings(
                        fixture.tenantId(),
                        fixture.guardianId()
                )
        );

        assertEquals(
                notificationsBefore,
                countActivationNotifications(
                        fixture.tenantId(),
                        fixture.guardianPhone()
                )
        );

        assertEquals(
                securePayloadsBefore,
                countSecurePayloads(
                        fixture.tenantId()
                )
        );
    }

    private Fixture createFixture() {

        String suffix =
                shortId(
                        UUID.randomUUID()
                );

        String auditUser =
                "b8-rollback-test";

        UUID tenantId =
                jdbc.queryForObject(
                        """
                        SELECT id
                        FROM eiam_tenant
                        WHERE code = 'GT-SCHOOL'
                        """,
                        UUID.class
                );

        Integer parentRoleCount =
                jdbc.queryForObject(
                        """
                        SELECT COUNT(*)
                        FROM eiam_role
                        WHERE tenant_id = ?
                          AND code = 'PARENT'
                          AND status = 'ACTIVE'
                        """,
                        Integer.class,
                        tenantId
                );

        assertEquals(
                1,
                parentRoleCount,
                "V155 PARENT role must exist for the GT-SCHOOL tenant"
        );

        UUID actorUserId =
                UUID.randomUUID();

        String actorEmail =
                "b8-actor-"
                        + suffix.toLowerCase()
                        + "@example.com";

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
                    ?,
                    CURRENT_TIMESTAMP,
                    ?,
                    0,
                    'ACTIVE'
                )
                """,
                actorUserId,
                tenantId,
                "b8-actor-" + suffix.toLowerCase(),
                actorEmail,
                "B8 Rollback Actor",
                "test-password-hash",
                auditUser,
                auditUser
        );

        UUID schoolProfileId =
                existingSchoolProfile(
                        tenantId
                );

        if (schoolProfileId == null) {
            schoolProfileId =
                    UUID.randomUUID();

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
                    "B8-SCH-" + suffix,
                    "B8 Rollback Test School",
                    auditUser,
                    auditUser
            );
        }

        UUID campusId =
                UUID.randomUUID();

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
                    FALSE,
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
                "B8-CAMP-" + suffix,
                "B8 Rollback Campus",
                auditUser,
                auditUser
        );

        UUID academicYearId =
                UUID.randomUUID();

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
                    FALSE,
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
                "B8-AY-" + suffix,
                "B8 Academic Year",
                Date.valueOf(
                        "2026-01-01"
                ),
                Date.valueOf(
                        "2026-12-31"
                ),
                auditUser,
                auditUser
        );

        UUID educationLevelId =
                UUID.randomUUID();

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
                "B8-PRIMARY-" + suffix,
                "B8 Primary",
                auditUser,
                auditUser
        );

        UUID classGradeId =
                UUID.randomUUID();

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
                "B8-P1-" + suffix,
                "B8 Primary One",
                auditUser,
                auditUser
        );

        UUID applicationId =
                UUID.randomUUID();

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
                "B8-APP-" + suffix,
                academicYearId,
                campusId,
                classGradeId,
                auditUser,
                auditUser
        );

        /*
         * No gts_admission_payment_obligation rows are inserted.
         *
         * This is a legitimate unlocked onboarding state verified by
         * AdmissionOnboardingGateServiceTest.
         */
        UUID guardianId =
                UUID.randomUUID();

        String guardianPhone =
                "+2567"
                        + digits(
                                suffix,
                                8
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
                    ?,
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
                guardianPhone,
                auditUser,
                auditUser
        );

        return new Fixture(
                tenantId,
                actorUserId,
                guardianId,
                guardianPhone
        );
    }

    private UUID existingSchoolProfile(
            UUID tenantId
    ) {
        return jdbc.query(
                        """
                        SELECT id
                        FROM gts_school_profile
                        WHERE tenant_id = ?
                        ORDER BY created_at
                        LIMIT 1
                        """,
                        (resultSet, rowNumber) ->
                                resultSet.getObject(
                                        "id",
                                        UUID.class
                                ),
                        tenantId
                )
                .stream()
                .findFirst()
                .orElse(
                        null
                );
    }

    private void setExecutionContext(
            UUID tenantId,
            UUID actorUserId
    ) {
        RequestContextHolder.set(
                new RequestContext(
                        "b8-rollback-" + UUID.randomUUID(),
                        tenantId.toString()
                )
        );

        GtPrincipal principal =
                new GtPrincipal(
                        actorUserId,
                        "b8-rollback-actor",
                        tenantId,
                        Set.of(
                                "SCHOOL_ADMIN"
                        ),
                        Set.of(),
                        UUID.randomUUID()
                );

        SecurityContextHolder
                .getContext()
                .setAuthentication(
                        UsernamePasswordAuthenticationToken
                                .authenticated(
                                        principal,
                                        "test-token",
                                        Set.of()
                                )
                );
    }

    private int countInvitationsByPhone(
            UUID tenantId,
            String phone
    ) {
        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM eiam_organization_invitation
                WHERE tenant_id = ?
                  AND phone_number = ?
                """,
                Integer.class,
                tenantId,
                phone
        );
    }

    private int countProvisionings(
            UUID tenantId,
            UUID guardianId
    ) {
        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM gts_admission_guardian_account_provisioning
                WHERE tenant_id = ?
                  AND admission_guardian_id = ?
                """,
                Integer.class,
                tenantId,
                guardianId
        );
    }

    private int countActivationNotifications(
            UUID tenantId,
            String recipient
    ) {
        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM ens_notification_requests
                WHERE tenant_id = ?
                  AND definition_code = 'GT-SCHOOL-PARENT-ACTIVATION'
                  AND recipient = ?
                """,
                Integer.class,
                tenantId,
                recipient
        );
    }

    private int countSecurePayloads(
            UUID tenantId
    ) {
        return jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM ens_notification_secure_payloads
                WHERE tenant_id = ?
                """,
                Integer.class,
                tenantId
        );
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

    /*
     * Converts the hexadecimal test suffix into deterministic digits
     * while preserving enough uniqueness for a canonical international
     * test phone number.
     */
    private static String digits(
            String value,
            int length
    ) {
        StringBuilder result =
                new StringBuilder();

        for (int index = 0; result.length() < length; index++) {
            char character =
                    value.charAt(
                            index % value.length()
                    );

            result.append(
                    Character.isDigit(
                            character
                    )
                            ? character
                            : (character - 'A') % 10
            );
        }

        return result.toString();
    }

    private record Fixture(
            UUID tenantId,
            UUID actorUserId,
            UUID guardianId,
            String guardianPhone
    ) {
    }
}
