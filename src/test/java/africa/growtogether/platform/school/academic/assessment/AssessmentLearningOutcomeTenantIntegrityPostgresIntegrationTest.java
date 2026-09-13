package africa.growtogether.platform.school.academic.assessment;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
class AssessmentLearningOutcomeTenantIntegrityPostgresIntegrationTest {

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
    void v189TenantIntegrityConstraintsExist() {

        assertConstraintExists(
                "uq_gts_learning_outcome_tenant_id"
        );

        assertConstraintExists(
                "fk_gts_assessment_learning_outcome_tenant"
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
