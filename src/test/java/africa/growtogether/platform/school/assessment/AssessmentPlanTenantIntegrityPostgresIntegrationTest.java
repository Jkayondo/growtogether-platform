package africa.growtogether.platform.school.assessment;

import org.junit.jupiter.api.Test;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
class AssessmentPlanTenantIntegrityPostgresIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres =
            new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("gt_assessment_plan_integrity")
                    .withUsername("gt")
                    .withPassword("gt");

    @Test
    void flywayAppliesV192AndCreatesAllNineTenantAwareForeignKeys() throws Exception {
        try (Connection connection = DriverManager.getConnection(
                postgres.getJdbcUrl(),
                postgres.getUsername(),
                postgres.getPassword());
             Statement statement = connection.createStatement()) {

            statement.execute("""
                    CREATE EXTENSION IF NOT EXISTS pgcrypto
                    """);

            org.flywaydb.core.Flyway.configure()
                    .dataSource(
                            postgres.getJdbcUrl(),
                            postgres.getUsername(),
                            postgres.getPassword())
                    .locations("classpath:db/migration")
                    .load()
                    .migrate();

            try (ResultSet rs = statement.executeQuery("""
                    SELECT COUNT(*)
                    FROM pg_constraint
                    WHERE conrelid = 'gts_assessment_plan'::regclass
                      AND contype = 'f'
                      AND conname IN (
                        'fk_gts_assessment_plan_academic_year_tenant',
                        'fk_gts_assessment_plan_academic_term_tenant',
                        'fk_gts_assessment_plan_campus_tenant',
                        'fk_gts_assessment_plan_academic_programme_tenant',
                        'fk_gts_assessment_plan_study_track_tenant',
                        'fk_gts_assessment_plan_curriculum_version_tenant',
                        'fk_gts_assessment_plan_class_grade_tenant',
                        'fk_gts_assessment_plan_stream_tenant',
                        'fk_gts_assessment_plan_grading_scheme_tenant'
                      )
                    """)) {
                assertTrue(rs.next());
                assertEquals(9, rs.getInt(1));
            }
        }
    }
}
