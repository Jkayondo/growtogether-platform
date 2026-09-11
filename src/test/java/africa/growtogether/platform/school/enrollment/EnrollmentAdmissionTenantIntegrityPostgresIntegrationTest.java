package africa.growtogether.platform.school.enrollment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
@Transactional
class EnrollmentAdmissionTenantIntegrityPostgresIntegrationTest {

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
    void v180ToV182TenantIntegrityConstraintsExist() {

        assertConstraintExists("uq_gts_student_tenant_id");
        assertConstraintExists("uq_gts_student_enrollment_tenant_id");

        assertConstraintExists("fk_gts_student_enrollment_student_tenant");
        assertConstraintExists("fk_gts_student_enrollment_academic_year_tenant");
        assertConstraintExists("fk_gts_student_enrollment_academic_term_tenant");
        assertConstraintExists("fk_gts_student_enrollment_campus_tenant");
        assertConstraintExists("fk_gts_student_enrollment_class_grade_tenant");
        assertConstraintExists("fk_gts_student_enrollment_stream_tenant");
        assertConstraintExists("fk_gts_student_enrollment_previous_enrollment_tenant");

        assertConstraintExists("uq_gts_admission_application_tenant_id");
        assertConstraintExists("fk_gts_student_admission_application_tenant");

        assertConstraintExists("fk_gts_admission_application_academic_year_tenant");
        assertConstraintExists("fk_gts_admission_application_campus_tenant");
        assertConstraintExists("fk_gts_admission_application_desired_class_grade_tenant");
        assertConstraintExists("fk_gts_admission_application_desired_stream_tenant");
        assertConstraintExists("fk_gts_admission_application_offered_class_grade_tenant");
        assertConstraintExists("fk_gts_admission_application_offered_stream_tenant");
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
