package africa.growtogether.platform.eiam.permission;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Testcontainers
@SpringBootTest
class SchoolAssessmentAuthorizationPostgresIntegrationTest {

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
    void assessmentPermissionsAreSeededForGtSchool() {

        List<String> permissions = jdbc.queryForList(
                """
                SELECT p.code
                FROM eiam_permission p
                JOIN eiam_tenant t
                  ON t.id = p.tenant_id
                WHERE t.code = 'GT-SCHOOL'
                  AND p.code IN (
                    'school.academic.assessment.create',
                    'school.academic.assessment.read',
                    'school.academic.assessment.manage'
                  )
                  AND p.status = 'ACTIVE'
                ORDER BY p.code
                """,
                String.class
        );

        assertEquals(
                List.of(
                        "school.academic.assessment.create",
                        "school.academic.assessment.manage",
                        "school.academic.assessment.read"
                ),
                permissions
        );
    }

    @Test
    void schoolAdminReceivesAllAssessmentPermissions() {

        Integer count = jdbc.queryForObject(
                """
                SELECT COUNT(*)
                FROM eiam_role_permission rp
                JOIN eiam_tenant t
                  ON t.id = rp.tenant_id
                JOIN eiam_role r
                  ON r.id = rp.role_id
                JOIN eiam_permission p
                  ON p.id = rp.permission_id
                WHERE t.code = 'GT-SCHOOL'
                  AND r.code = 'SCHOOL_ADMIN'
                  AND p.code IN (
                    'school.academic.assessment.create',
                    'school.academic.assessment.read',
                    'school.academic.assessment.manage'
                  )
                  AND rp.status = 'ACTIVE'
                """,
                Integer.class
        );

        assertEquals(3, count);
    }
}
