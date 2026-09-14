package africa.growtogether.platform.school.assessment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
class AssessmentConfigurationTenantIntegrityPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:17-alpine");

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    JdbcTemplate jdbc;

    @Test
    void v191ConstraintsExist() {
        assertConstraintExists(
                "uq_subject_configurations_tenant_id"
        );
        assertConstraintExists(
                "fk_assessment_configurations_subject_tenant"
        );
        assertConstraintExists(
                "uq_assessment_configurations_tenant_subject_name"
        );
    }

    @Test
    void assessmentConfigurationRejectsCrossTenantSubject() {
        UUID tenantA = UUID.randomUUID();
        UUID tenantB = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        insertSubjectConfiguration(
                subjectId,
                tenantA,
                "Mathematics"
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> jdbc.update(
                        """
                        INSERT INTO assessment_configurations (
                            id,
                            tenant_id,
                            subject_configuration_id,
                            assessment_type,
                            assessment_name,
                            weight_percentage,
                            status,
                            version
                        )
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                        """,
                        UUID.randomUUID(),
                        tenantB,
                        subjectId,
                        "EXAM",
                        "End of Term Examination",
                        100,
                        "ACTIVE",
                        0L
                )
        );
    }

    @Test
    void assessmentConfigurationRejectsDuplicateNameWithinTenantAndSubject() {
        UUID tenantId = UUID.randomUUID();
        UUID subjectId = UUID.randomUUID();

        insertSubjectConfiguration(
                subjectId,
                tenantId,
                "English"
        );

        insertAssessmentConfiguration(
                UUID.randomUUID(),
                tenantId,
                subjectId,
                "EXAM",
                "End of Term Examination",
                100
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> insertAssessmentConfiguration(
                        UUID.randomUUID(),
                        tenantId,
                        subjectId,
                        "TEST",
                        "End of Term Examination",
                        50
                )
        );
    }

    private void insertSubjectConfiguration(
            UUID id,
            UUID tenantId,
            String subjectName
    ) {
        jdbc.update(
                """
                INSERT INTO subject_configurations (
                    id,
                    tenant_id,
                    academic_grade_id,
                    subject_name,
                    subject_code,
                    mandatory,
                    status,
                    version
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                tenantId,
                UUID.randomUUID(),
                subjectName,
                "TEST-" + id.toString().substring(0, 8),
                true,
                "ACTIVE",
                0L
        );
    }

    private void insertAssessmentConfiguration(
            UUID id,
            UUID tenantId,
            UUID subjectId,
            String assessmentType,
            String assessmentName,
            int weightPercentage
    ) {
        jdbc.update(
                """
                INSERT INTO assessment_configurations (
                    id,
                    tenant_id,
                    subject_configuration_id,
                    assessment_type,
                    assessment_name,
                    weight_percentage,
                    status,
                    version
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?)
                """,
                id,
                tenantId,
                subjectId,
                assessmentType,
                assessmentName,
                weightPercentage,
                "ACTIVE",
                0L
        );
    }

    private void assertConstraintExists(String constraintName) {
        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM pg_constraint
                WHERE conname = ?
                """,
                Integer.class,
                constraintName
        );

        assertTrue(
                count != null && count > 0,
                "Expected PostgreSQL constraint: " + constraintName
        );
    }
}
